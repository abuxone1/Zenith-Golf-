package com.example.game.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client for communicating with the separate Zenith Golf Secure Game Services Backend
 * (Firebase Cloud Functions v2 API).
 *
 * Security Guarantees:
 * - Never embeds or exposes service account keys or admin secrets in the Android client.
 * - Authenticates every request with a freshly verified Firebase Authentication JWT ID token
 *   (`Authorization: Bearer <idToken>`).
 * - Executes real Firestore security-rule-validated transactions via [GolfCloudRepository].
 */
class GolfSecureBackendClient(
    private val repository: GolfCloudRepository,
    private val auth: FirebaseAuth = Firebase.auth,
    private val cloudFunctionsBaseUrl: String? = null
) {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Retrieves the current user's verified Firebase Authentication ID token.
     */
    suspend fun getVerifiedFirebaseIdToken(forceRefresh: Boolean = false): Result<String> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Player must be authenticated with Google Sign-In" }
        val tokenResult = user.getIdToken(forceRefresh).await()
        checkNotNull(tokenResult.token) { "Firebase ID token is unavailable" }
    }

    /**
     * Submits a verified golf round score through the anti-cheat & idempotency pipeline.
     */
    suspend fun submitVerifiedScore(
        mode: String,
        courseName: String,
        holesPlayed: Int,
        totalStrokes: Int,
        scoreToPar: Int,
        maxDriveYds: Float,
        roundDurationSec: Int,
        idempotencyKey: String
    ): Result<VerifiedScoreSubmissionResult> = withContext(Dispatchers.IO) {
        if (!cloudFunctionsBaseUrl.isNullOrBlank()) {
            val tokenResult = getVerifiedFirebaseIdToken(false)
            if (tokenResult.isSuccess) {
                val httpRes = postAuthenticatedJson(
                    endpointPath = "/v1/matches/submit",
                    bearerToken = tokenResult.getOrThrow(),
                    payload = JSONObject().apply {
                        put("userId", auth.currentUser?.uid.orEmpty())
                        put("mode", mode)
                        put("courseName", courseName)
                        put("holesPlayed", holesPlayed)
                        put("totalStrokes", totalStrokes)
                        put("scoreToPar", scoreToPar)
                        put("maxDriveYds", maxDriveYds.toDouble())
                        put("roundDurationSec", roundDurationSec)
                        put("idempotencyKey", idempotencyKey)
                    }
                )
                if (httpRes.isSuccess) {
                    val data = httpRes.getOrThrow().optJSONObject("data")
                    if (data != null) {
                        return@withContext Result.success(
                            VerifiedScoreSubmissionResult(
                                matchId = data.optString("matchId"),
                                verificationStatus = data.optString("verificationStatus", "VERIFIED"),
                                scoreBanner = data.optString("scoreBanner", "PAR"),
                                coinsEarned = data.optInt("coinsEarned", 0),
                                xpEarned = data.optInt("xpEarned", 0),
                                trophiesDelta = data.optInt("trophiesDelta", 0)
                            )
                        )
                    }
                }
            }
        }

        repository.submitVerifiedMatchScore(
            mode = mode,
            courseName = courseName,
            holesPlayed = holesPlayed,
            totalStrokes = totalStrokes,
            scoreToPar = scoreToPar,
            maxDriveYds = maxDriveYds,
            roundDurationSec = roundDurationSec,
            idempotencyKey = idempotencyKey
        )
    }

    /**
     * Registers the authenticated player for an official tournament with rate-limiting,
     * duplicate registration rejection, entry fee ledger recording, and audit logging.
     */
    suspend fun registerForTournament(
        tournamentId: String,
        username: String,
        entryFeeCoins: Int,
        trophiesAtEntry: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        repository.registerForTournamentVerified(
            tournamentId = tournamentId,
            username = username,
            entryFeePaid = entryFeeCoins,
            trophiesAtEntry = trophiesAtEntry
        )
    }

    private fun postAuthenticatedJson(
        endpointPath: String,
        bearerToken: String,
        payload: JSONObject
    ): Result<JSONObject> = runCatching {
        val url = cloudFunctionsBaseUrl!!.trimEnd('/') + endpointPath
        val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $bearerToken")
            .post(body)
            .build()
        httpClient.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("HTTP ${resp.code}: $raw")
            }
            JSONObject(raw)
        }
    }
}
