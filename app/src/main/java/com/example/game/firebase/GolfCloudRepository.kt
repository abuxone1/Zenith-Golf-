package com.example.game.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.game.model.AiDifficultyTier
import com.example.game.model.ClubType
import com.example.game.model.ClubUpgrade
import com.example.game.model.DailyChallenge
import com.example.game.model.GolfBallSkin
import com.example.game.model.GolferProfile
import com.example.game.model.GolferStance
import com.example.game.model.GolferSwingStyle
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.PlayAchievement
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.PlayerCareer
import com.example.game.model.TourPassState
import com.example.game.model.TournamentCup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?,
    auth: FirebaseAuth = Firebase.auth
): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

/**
 * Snapshot of resolved player state after non-destructive local + cloud conflict resolution.
 */
data class ResolvedCloudSyncState(
    val career: PlayerCareer,
    val balls: List<GolfBallSkin>,
    val clubs: List<ClubUpgrade>,
    val tourPassState: TourPassState,
    val golferProfile: GolferProfile,
    val playSettings: PlayConsoleSettings,
    val unlockedCourseIds: List<String>,
    val unlockedAchievementIds: Set<String>,
    val completedChallengeIds: Set<String>,
    val claimedChallengeIds: Set<String>,
    val syncRevision: Long
)

/**
 * Repository layer connecting Zenith Golf 2.0 to Cloud Firestore across all 12 domain collections:
 * 1. `players`
 * 2. `player_statistics`
 * 3. `player_inventory`
 * 4. `player_progress`
 * 5. `game_settings`
 * 6. `match_history`
 * 7. `tournaments`
 * 8. `tournament_entries`
 * 9. `achievements`
 * 10. `daily_challenges`
 * 11. `reward_transactions`
 * 12. `leaderboards`
 * (Plus `/users/{userId}` and `/users/{userId}/tourPass/state` for full backwards compatibility).
 */
class GolfCloudRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // =========================================================================
    // LEGACY / UNIFIED USER PROFILE & TOUR PASS (`/users/{userId}`)
    // =========================================================================

    fun observeUserProfile(userId: String = currentUserId.orEmpty()): Flow<CloudUserProfile?> {
        val docRef = db.collection("users").document(userId.ifBlank { "unauthenticated" })
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
    }

    suspend fun getUserProfileById(userId: String): Result<CloudUserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun saveUserProfile(
        userId: String,
        career: PlayerCareer,
        displayName: String = "Pro Golfer",
        email: String = "",
        balls: List<GolfBallSkin> = emptyList(),
        clubs: List<ClubUpgrade> = emptyList(),
        golferName: String = "Apex Pro"
    ): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("users").document(userId)

        val unlockedIds = balls
            .filter { it.isUnlocked }
            .map { it.id.take(64) }
            .ifEmpty { listOf("standard_white") }
            .take(30)

        fun levelFor(type: ClubType): Long =
            (clubs.firstOrNull { it.clubType == type }?.level ?: 1).toLong().coerceIn(1L, 10L)

        val profile = CloudUserProfile(
            id = userId,
            userId = uid,
            displayName = displayName.take(80),
            email = email.take(160),
            coins = career.coins.toLong().coerceAtLeast(0L),
            trophies = career.trophies.toLong().coerceAtLeast(0L),
            mulligansRemaining = career.mulligansRemaining.toLong().coerceAtLeast(0L),
            roundsPlayed = career.roundsPlayed.toLong().coerceAtLeast(0L),
            holesPlayed = career.holesPlayed.toLong().coerceAtLeast(0L),
            birdies = career.birdies.toLong().coerceAtLeast(0L),
            eagles = career.eagles.toLong().coerceAtLeast(0L),
            aces = career.aces.toLong().coerceAtLeast(0L),
            longestDriveYds = career.longestDriveYds.toDouble().coerceAtLeast(0.0),
            tourXp = career.tourXp.toLong().coerceAtLeast(0L),
            shootoutHighScore = career.shootoutHighScore.toLong().coerceAtLeast(0L),
            equippedBallId = career.equippedBallId.take(64),
            equippedTracer = career.equippedTracer.take(64),
            unlockedBallIds = unlockedIds,
            driverLevel = levelFor(ClubType.DRIVER),
            wood3Level = levelFor(ClubType.WOOD_3),
            iron7Level = levelFor(ClubType.IRON_7),
            wedgeLevel = levelFor(ClubType.PITCHING_WEDGE),
            putterLevel = levelFor(ClubType.PUTTER),
            golferName = golferName.take(64),
            updatedAt = null
        )

        try {
            docRef.set(profile).await()
            Log.d(TAG, "User profile successfully saved to cloud")
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    fun observeTourPass(userId: String = currentUserId.orEmpty()): Flow<CloudTourPass?> {
        val docRef = db.collection("users").document(userId.ifBlank { "unauthenticated" })
            .collection("tourPass").document("state")
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(CloudTourPass::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
    }

    suspend fun saveTourPass(userId: String, state: TourPassState): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("users").document(userId)
            .collection("tourPass").document("state")

        val cloudPass = CloudTourPass(
            id = "state",
            userId = uid,
            currentXp = state.currentXp.toLong().coerceAtLeast(0L),
            claimedFreeTiers = state.claimedFreeTiers.take(50).map { it.toLong() },
            claimedVipTiers = state.claimedVipTiers.take(50).map { it.toLong() },
            isVipUnlocked = state.isVipUnlocked,
            updatedAt = null
        )

        try {
            docRef.set(cloudPass).await()
            "state"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 1. COLLECTION: `players` (`/players/{userId}`)
    // =========================================================================

    fun observePlayerDocument(userId: String = currentUserId.orEmpty()): Flow<CloudPlayerDocument?> {
        val docRef = db.collection("players").document(userId.ifBlank { "unauthenticated" })
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(CloudPlayerDocument::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        }
    }

    suspend fun getPlayerDocument(userId: String): Result<CloudPlayerDocument?> = runCatching {
        val docRef = db.collection("players").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudPlayerDocument::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun savePlayerDocument(
        userId: String,
        career: PlayerCareer,
        golferProfile: GolferProfile,
        email: String = "",
        profileImageUrl: String = "",
        clientTimestampMs: Long = System.currentTimeMillis()
    ): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("players").document(userId)
        val computedLevel = ((career.tourXp / 250) + 1).toLong().coerceIn(1L, 500L)

        val doc = CloudPlayerDocument(
            id = userId,
            userId = uid,
            username = golferProfile.golferName.ifBlank { "Apex Pro" }.take(80),
            profileImageUrl = profileImageUrl.take(500),
            email = email.take(160),
            playerLevel = computedLevel,
            xp = career.tourXp.toLong().coerceAtLeast(0L),
            coins = career.coins.toLong().coerceAtLeast(0L),
            trophies = career.trophies.toLong().coerceAtLeast(0L),
            mulligansRemaining = career.mulligansRemaining.toLong().coerceAtLeast(0L),
            countryTag = golferProfile.countryTag.take(64),
            accountTier = "GOOGLE_LINKED",
            clientUpdatedAtEpochMs = clientTimestampMs.coerceAtLeast(0L),
            updatedAt = null
        )

        try {
            docRef.set(doc).await()
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 2. COLLECTION: `player_statistics` (`/player_statistics/{userId}`)
    // =========================================================================

    suspend fun getPlayerStatistics(userId: String): Result<CloudPlayerStatistics?> = runCatching {
        val docRef = db.collection("player_statistics").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudPlayerStatistics::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun savePlayerStatistics(userId: String, career: PlayerCareer): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("player_statistics").document(userId)
        val stats = CloudPlayerStatistics(
            id = userId,
            userId = uid,
            roundsPlayed = career.roundsPlayed.toLong().coerceAtLeast(0L),
            holesPlayed = career.holesPlayed.toLong().coerceAtLeast(0L),
            birdies = career.birdies.toLong().coerceAtLeast(0L),
            eagles = career.eagles.toLong().coerceAtLeast(0L),
            aces = career.aces.toLong().coerceAtLeast(0L),
            longestDriveYds = career.longestDriveYds.toDouble().coerceAtLeast(0.0),
            greensInRegulationCount = career.greensInRegulationCount.toLong().coerceAtLeast(0L),
            shootoutHighScore = career.shootoutHighScore.toLong().coerceAtLeast(0L),
            matchPlayWins = career.matchPlayWins.toLong().coerceAtLeast(0L),
            matchPlayLosses = career.matchPlayLosses.toLong().coerceAtLeast(0L),
            updatedAt = null
        )

        try {
            docRef.set(stats).await()
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 3. COLLECTION: `player_inventory` (`/player_inventory/{userId}`)
    // =========================================================================

    suspend fun getPlayerInventory(userId: String): Result<CloudPlayerInventory?> = runCatching {
        val docRef = db.collection("player_inventory").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudPlayerInventory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun savePlayerInventory(
        userId: String,
        career: PlayerCareer,
        balls: List<GolfBallSkin>,
        clubs: List<ClubUpgrade>,
        golferProfile: GolferProfile = GolferProfile()
    ): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("player_inventory").document(userId)

        val unlockedIds = balls
            .filter { it.isUnlocked }
            .map { it.id.take(64) }
            .ifEmpty { listOf("standard_white") }
            .take(30)

        fun levelFor(type: ClubType): Long =
            (clubs.firstOrNull { it.clubType == type }?.level ?: 1).toLong().coerceIn(1L, 10L)

        val inventory = CloudPlayerInventory(
            id = userId,
            userId = uid,
            equippedBallId = career.equippedBallId.ifBlank { "standard_white" }.take(64),
            equippedTracer = career.equippedTracer.ifBlank { "Cyan Surge" }.take(64),
            unlockedBallIds = unlockedIds,
            driverLevel = levelFor(ClubType.DRIVER),
            wood3Level = levelFor(ClubType.WOOD_3),
            iron4Level = levelFor(ClubType.IRON_4),
            iron7Level = levelFor(ClubType.IRON_7),
            iron9Level = levelFor(ClubType.IRON_9),
            wedgeLevel = levelFor(ClubType.PITCHING_WEDGE),
            sandWedgeLevel = levelFor(ClubType.SAND_WEDGE),
            putterLevel = levelFor(ClubType.PUTTER),
            visorColorHex = golferProfile.visorColorHex,
            poloColorHex = golferProfile.poloColorHex,
            gloveStyle = golferProfile.gloveStyle.take(64),
            stance = golferProfile.stance.name.take(32),
            swingStyle = golferProfile.swingStyle.name.take(48),
            updatedAt = null
        )

        try {
            docRef.set(inventory).await()
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 4. COLLECTION: `player_progress` (`/player_progress/{userId}`)
    // =========================================================================

    suspend fun getPlayerProgress(userId: String): Result<CloudPlayerProgress?> = runCatching {
        val docRef = db.collection("player_progress").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudPlayerProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun savePlayerProgress(
        userId: String,
        career: PlayerCareer,
        tourPass: TourPassState,
        unlockedCourses: List<String>,
        achievements: List<PlayAchievement>,
        dailyChallenges: List<DailyChallenge>,
        syncRevision: Long = 1L
    ): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("player_progress").document(userId)
        val computedLevel = ((career.tourXp / 250) + 1).toLong().coerceIn(1L, 500L)

        val progress = CloudPlayerProgress(
            id = userId,
            userId = uid,
            playerLevel = computedLevel,
            tourXp = maxOf(career.tourXp, tourPass.currentXp).toLong().coerceAtLeast(0L),
            trophies = career.trophies.toLong().coerceAtLeast(0L),
            unlockedCourses = unlockedCourses.map { it.take(64) }.ifEmpty { listOf("cup_emerald_links") }.take(30),
            unlockedAchievementIds = achievements.filter { it.isUnlocked }.map { it.id.take(64) }.take(50),
            completedChallengeIds = dailyChallenges.filter { it.isCompleted }.map { it.id.take(64) }.take(50),
            claimedChallengeIds = dailyChallenges.filter { it.isClaimed }.map { it.id.take(64) }.take(50),
            claimedFreeTiers = tourPass.claimedFreeTiers.take(50).map { it.toLong() },
            claimedVipTiers = tourPass.claimedVipTiers.take(50).map { it.toLong() },
            isVipUnlocked = tourPass.isVipUnlocked,
            syncRevision = syncRevision.coerceAtLeast(1L),
            updatedAt = null
        )

        try {
            docRef.set(progress).await()
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 5. COLLECTION: `game_settings` (`/game_settings/{userId}`)
    // =========================================================================

    suspend fun getGameSettings(userId: String): Result<CloudGameSettings?> = runCatching {
        val docRef = db.collection("game_settings").document(userId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(CloudGameSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun saveGameSettings(userId: String, settings: PlayConsoleSettings): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val docRef = db.collection("game_settings").document(userId)
        val cloudSettings = CloudGameSettings(
            id = userId,
            userId = uid,
            isSoundEnabled = settings.isSoundEnabled,
            soundVolume = settings.soundVolume.toDouble().coerceIn(0.0, 1.0),
            isHapticsEnabled = settings.isHapticsEnabled,
            isCaddieAudioTipsEnabled = settings.isCaddieAudioTipsEnabled,
            isHighFpsMode = settings.isHighFpsMode,
            isPuttingGridVisible = settings.isPuttingGridVisible,
            isSlopeBreakRadarVisible = settings.isSlopeBreakRadarVisible,
            isCloudSyncEnabled = settings.isCloudSyncEnabled,
            graphicsTier = settings.graphicsTier.name.take(32),
            aiDifficulty = settings.aiDifficulty.name.take(32),
            updatedAt = null
        )

        try {
            docRef.set(cloudSettings).await()
            userId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    // =========================================================================
    // 6. COLLECTION: `match_history` (`/match_history/{matchId}`)
    // =========================================================================

    suspend fun recordMatchHistory(
        mode: String,
        courseName: String,
        holesPlayed: Int,
        totalStrokes: Int,
        scoreToPar: Int,
        scoreBanner: String,
        coinsEarned: Int,
        xpEarned: Int,
        opponentName: String = "Tour Field",
        outcome: String = "COMPLETED",
        matchId: String = "match_${UUID.randomUUID().toString().replace("-", "").take(20)}"
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be authenticated to record match history" }
        val docRef = db.collection("match_history").document(matchId)

        val record = CloudMatchHistoryRecord(
            id = matchId,
            userId = uid,
            mode = mode.ifBlank { "TOURNAMENT" }.take(40),
            courseName = courseName.ifBlank { "Zenith Links" }.take(80),
            holesPlayed = holesPlayed.toLong().coerceIn(1L, 18L),
            totalStrokes = totalStrokes.toLong().coerceIn(1L, 200L),
            scoreToPar = scoreToPar.toLong().coerceIn(-36L, 100L),
            scoreBanner = scoreBanner.take(48),
            coinsEarned = coinsEarned.toLong().coerceIn(0L, 50000L),
            xpEarned = xpEarned.toLong().coerceIn(0L, 50000L),
            opponentName = opponentName.take(80),
            outcome = outcome.ifBlank { "COMPLETED" }.take(32),
            createdAt = null
        )

        try {
            docRef.set(record).await()
            matchId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getUserMatchHistory(userId: String = currentUserId.orEmpty()): Result<List<CloudMatchHistoryRecord>> = runCatching {
        val path = "match_history"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", userId)
                .limit(25)
                .get()
                .await()
            snapshot.toObjects(CloudMatchHistoryRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    fun observeUserMatchHistory(userId: String = currentUserId.orEmpty()): Flow<List<CloudMatchHistoryRecord>> {
        return db.collection("match_history")
            .whereEqualTo("userId", userId.ifBlank { "unauthenticated" })
            .limit(25)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CloudMatchHistoryRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
    }

    // =========================================================================
    // 7. COLLECTION: `tournaments` (`/tournaments/{tournamentId}`)
    // =========================================================================

    suspend fun upsertTournament(cup: TournamentCup, status: String = "OPEN"): Result<String> = runCatching {
        val safeId = cup.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifBlank { "cup_default" }
        val docRef = db.collection("tournaments").document(safeId)

        val doc = CloudTournamentDocument(
            id = safeId,
            tournamentId = safeId,
            title = cup.title.ifBlank { "Zenith Cup" }.take(80),
            courseName = cup.courseName.ifBlank { "Zenith Championship Links" }.take(80),
            entryFeeCoins = cup.entryFeeCoins.toLong().coerceIn(0L, 100000L),
            prizeCoins = cup.prizeCoins.toLong().coerceIn(0L, 500000L),
            prizeTrophies = cup.prizeTrophies.toLong().coerceIn(0L, 1000L),
            holesCount = cup.holesCount.toLong().coerceIn(1L, 18L),
            difficulty = cup.difficulty.ifBlank { "Pro" }.take(32),
            status = status.take(32),
            updatedAt = null
        )

        try {
            docRef.set(doc).await()
            safeId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getActiveTournaments(): Result<List<CloudTournamentDocument>> = runCatching {
        val path = "tournaments"
        try {
            val snapshot = db.collection(path)
                .whereGreaterThanOrEqualTo("prizeCoins", 0L)
                .limit(20)
                .get()
                .await()
            snapshot.toObjects(CloudTournamentDocument::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    // =========================================================================
    // 8. COLLECTION: `tournament_entries` (`/tournament_entries/{entryId}`)
    // =========================================================================

    suspend fun submitTournamentEntry(
        tournamentId: String,
        username: String,
        strokes: Int,
        scoreToPar: Int,
        holesCompleted: Int,
        rankPosition: Int = 1,
        rewardCoinsAwarded: Int = 0,
        status: String = "COMPLETED"
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to submit tournament entry" }
        val safeTourId = tournamentId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40).ifBlank { "zenith_open" }
        val entryId = "${safeTourId}_${uid}".take(120)
        val docRef = db.collection("tournament_entries").document(entryId)

        val entry = CloudTournamentEntry(
            id = entryId,
            userId = uid,
            tournamentId = safeTourId,
            username = username.ifBlank { "Apex Pro" }.take(80),
            strokes = strokes.toLong().coerceIn(1L, 200L),
            scoreToPar = scoreToPar.toLong().coerceIn(-36L, 100L),
            holesCompleted = holesCompleted.toLong().coerceIn(1L, 18L),
            rankPosition = rankPosition.toLong().coerceIn(1L, 1000L),
            rewardCoinsAwarded = rewardCoinsAwarded.toLong().coerceIn(0L, 50000L),
            status = status.take(32),
            updatedAt = null
        )

        try {
            docRef.set(entry).await()
            entryId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getTournamentEntries(tournamentId: String): Result<List<CloudTournamentEntry>> = runCatching {
        val path = "tournament_entries"
        try {
            val snapshot = db.collection(path)
                .whereGreaterThanOrEqualTo("strokes", 1L)
                .orderBy("strokes", Query.Direction.ASCENDING)
                .limit(25)
                .get()
                .await()
            snapshot.toObjects(CloudTournamentEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .filter { it.tournamentId == tournamentId || tournamentId.isBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    // =========================================================================
    // 9. COLLECTION: `achievements` (`/achievements/{achievementDocId}`)
    // =========================================================================

    suspend fun saveAchievementRecord(userId: String, achievement: PlayAchievement): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val safeAchId = achievement.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(48)
        val docId = "${userId}_${safeAchId}".take(120)
        val docRef = db.collection("achievements").document(docId)

        val record = CloudAchievementRecord(
            id = docId,
            userId = uid,
            achievementId = safeAchId,
            title = achievement.title.ifBlank { "Achievement" }.take(80),
            description = achievement.description.take(200),
            xpValue = achievement.xpValue.toLong().coerceIn(0L, 25000L),
            isUnlocked = achievement.isUnlocked,
            unlockedAt = null
        )

        try {
            docRef.set(record).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getUserAchievements(userId: String = currentUserId.orEmpty()): Result<List<CloudAchievementRecord>> = runCatching {
        val path = "achievements"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(CloudAchievementRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    // =========================================================================
    // 10. COLLECTION: `daily_challenges` (`/daily_challenges/{challengeDocId}`)
    // =========================================================================

    suspend fun saveDailyChallengeRecord(userId: String, challenge: DailyChallenge): Result<String> = runCatching {
        val uid = currentUserId ?: userId
        val safeChId = challenge.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(48)
        val docId = "${userId}_${safeChId}".take(120)
        val docRef = db.collection("daily_challenges").document(docId)

        val record = CloudDailyChallengeRecord(
            id = docId,
            userId = uid,
            challengeId = safeChId,
            title = challenge.title.ifBlank { "Daily Mission" }.take(80),
            targetType = challenge.targetType.ifBlank { "GENERAL" }.take(48),
            currentProgress = challenge.currentProgress.toLong().coerceIn(0L, 1000L),
            targetGoal = challenge.targetGoal.toLong().coerceIn(1L, 1000L),
            rewardCoins = challenge.rewardCoins.toLong().coerceIn(0L, 10000L),
            rewardMulligans = challenge.rewardMulligans.toLong().coerceIn(0L, 50L),
            isCompleted = challenge.isCompleted,
            isClaimed = challenge.isClaimed,
            updatedAt = null
        )

        try {
            docRef.set(record).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getUserDailyChallenges(userId: String = currentUserId.orEmpty()): Result<List<CloudDailyChallengeRecord>> = runCatching {
        val path = "daily_challenges"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(CloudDailyChallengeRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    // =========================================================================
    // 11. COLLECTION: `reward_transactions` (`/reward_transactions/{txId}`)
    // =========================================================================

    suspend fun recordRewardTransaction(
        rewardType: String,
        sourceId: String,
        coinsDelta: Int,
        xpDelta: Int,
        mulligansDelta: Int = 0,
        txId: String = "tx_${UUID.randomUUID().toString().replace("-", "").take(20)}"
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to record reward transaction" }
        val docRef = db.collection("reward_transactions").document(txId)

        val tx = CloudRewardTransaction(
            id = txId,
            userId = uid,
            rewardType = rewardType.ifBlank { "GAME_REWARD" }.take(48),
            sourceId = sourceId.ifBlank { "zenith_event" }.take(80),
            coinsDelta = coinsDelta.toLong().coerceIn(-50000L, 50000L),
            xpDelta = xpDelta.toLong().coerceIn(0L, 50000L),
            mulligansDelta = mulligansDelta.toLong().coerceIn(-50L, 50L),
            createdAt = null
        )

        try {
            docRef.set(tx).await()
            txId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun getUserRewardTransactions(userId: String = currentUserId.orEmpty()): Result<List<CloudRewardTransaction>> = runCatching {
        val path = "reward_transactions"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", userId)
                .limit(30)
                .get()
                .await()
            snapshot.toObjects(CloudRewardTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path, auth)
            throw e
        }
    }

    // =========================================================================
    // 12. COLLECTION: `leaderboards` (`/leaderboards/{userId}` & `/leaderboards/global/entries/{userId}`)
    // =========================================================================

    suspend fun submitLeaderboardScore(
        displayName: String,
        trophies: Int,
        shootoutHighScore: Int,
        longestDriveYds: Float,
        playerLevel: Int = 1,
        bestTournamentScoreToPar: Int = 0
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to submit leaderboard score" }
        val topLevelRef = db.collection("leaderboards").document(uid)
        val boardScopedRef = db.collection("leaderboards").document("global")
            .collection("entries").document(uid)

        val entry = CloudLeaderboardEntry(
            id = uid,
            userId = uid,
            displayName = displayName.ifBlank { "Golfer" }.take(80),
            trophies = trophies.toLong().coerceAtLeast(0L),
            shootoutHighScore = shootoutHighScore.toLong().coerceAtLeast(0L),
            longestDriveYds = longestDriveYds.toDouble().coerceAtLeast(0.0),
            playerLevel = playerLevel.toLong().coerceIn(1L, 500L),
            bestTournamentScoreToPar = bestTournamentScoreToPar.toLong().coerceIn(-36L, 100L),
            updatedAt = null
        )

        try {
            topLevelRef.set(entry).await()
            boardScopedRef.set(entry).await()
            uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, boardScopedRef.path, auth)
            throw e
        }
    }

    fun observeLeaderboard(): Flow<List<CloudLeaderboardEntry>> {
        return db.collection("leaderboards").document("global")
            .collection("entries")
            .whereGreaterThanOrEqualTo("trophies", 0L)
            .orderBy("trophies", Query.Direction.DESCENDING)
            .limit(25)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CloudLeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
    }

    // =========================================================================
    // CONFLICT-FREE MULTI-DEVICE SYNCHRONIZATION & PROGRESS RESTORATION
    // =========================================================================

    /**
     * Synchronizes all player collections in Cloud Firestore (`players`, `player_statistics`,
     * `player_inventory`, `player_progress`, `game_settings`, `achievements`, `daily_challenges`,
     * `leaderboards`, and `/users/{userId}`).
     */
    suspend fun syncFullPlayerState(
        userId: String,
        email: String,
        displayName: String,
        profileImageUrl: String = "",
        career: PlayerCareer,
        balls: List<GolfBallSkin>,
        clubs: List<ClubUpgrade>,
        tourPass: TourPassState,
        golferProfile: GolferProfile,
        playSettings: PlayConsoleSettings,
        unlockedCourses: List<String>,
        achievements: List<PlayAchievement>,
        dailyChallenges: List<DailyChallenge>,
        syncRevision: Long = 1L
    ): Result<Unit> = runCatching {
        saveUserProfile(
            userId = userId,
            career = career,
            displayName = displayName,
            email = email,
            balls = balls,
            clubs = clubs,
            golferName = golferProfile.golferName
        ).getOrThrow()

        saveTourPass(userId, tourPass).getOrThrow()

        savePlayerDocument(
            userId = userId,
            career = career,
            golferProfile = golferProfile,
            email = email,
            profileImageUrl = profileImageUrl
        ).getOrThrow()

        savePlayerStatistics(userId, career).getOrThrow()

        savePlayerInventory(
            userId = userId,
            career = career,
            balls = balls,
            clubs = clubs,
            golferProfile = golferProfile
        ).getOrThrow()

        savePlayerProgress(
            userId = userId,
            career = career,
            tourPass = tourPass,
            unlockedCourses = unlockedCourses,
            achievements = achievements,
            dailyChallenges = dailyChallenges,
            syncRevision = syncRevision
        ).getOrThrow()

        saveGameSettings(userId, playSettings).getOrThrow()

        val computedLevel = ((career.tourXp / 250) + 1).coerceIn(1, 500)
        submitLeaderboardScore(
            displayName = golferProfile.golferName.ifBlank { displayName },
            trophies = career.trophies,
            shootoutHighScore = career.shootoutHighScore,
            longestDriveYds = career.longestDriveYds,
            playerLevel = computedLevel
        ).getOrThrow()
    }

    /**
     * Resolves synchronization conflicts between local device state and Cloud Firestore documents
     * without overwriting newer progress.
     * - Monotonic career counters (XP, trophies, holesPlayed, roundsPlayed, birdies, eagles, aces,
     *   longestDriveYds, club levels) take `maxOf(local, cloud)`.
     * - Unlocked sets (golf balls, courses, achievements, completed/claimed challenges, Tour Pass tiers)
     *   take the set union `(local + cloud).distinct()`.
     * - Mutable profile/settings fields prefer the newer snapshot based on `clientUpdatedAtEpochMs` / `updatedAt`.
     */
    fun resolveSyncConflict(
        localCareer: PlayerCareer,
        localBalls: List<GolfBallSkin>,
        localClubs: List<ClubUpgrade>,
        localTourPass: TourPassState,
        localGolfer: GolferProfile,
        localSettings: PlayConsoleSettings,
        localUnlockedCourses: List<String>,
        localUnlockedAchievements: Set<String>,
        localCompletedChallenges: Set<String>,
        localClaimedChallenges: Set<String>,
        localUpdatedAtEpochMs: Long = 0L,
        cloudPlayer: CloudPlayerDocument? = null,
        cloudStats: CloudPlayerStatistics? = null,
        cloudInventory: CloudPlayerInventory? = null,
        cloudProgress: CloudPlayerProgress? = null,
        cloudSettings: CloudGameSettings? = null,
        cloudLegacyProfile: CloudUserProfile? = null
    ): ResolvedCloudSyncState {
        val cloudEpochMs = maxOf(
            cloudPlayer?.clientUpdatedAtEpochMs ?: 0L,
            (cloudPlayer?.updatedAt?.seconds ?: 0L) * 1000L,
            (cloudLegacyProfile?.updatedAt?.seconds ?: 0L) * 1000L
        )
        val isCloudNewer = cloudEpochMs >= localUpdatedAtEpochMs && (cloudPlayer != null || cloudLegacyProfile != null)

        val cloudCoins = maxOf(cloudPlayer?.coins ?: 0L, cloudLegacyProfile?.coins ?: 0L).toInt()
        val cloudTrophies = maxOf(
            cloudPlayer?.trophies ?: 0L,
            cloudProgress?.trophies ?: 0L,
            cloudLegacyProfile?.trophies ?: 0L
        ).toInt()
        val cloudMulligans = maxOf(
            cloudPlayer?.mulligansRemaining ?: 0L,
            cloudLegacyProfile?.mulligansRemaining ?: 0L
        ).toInt()
        val cloudXp = maxOf(
            cloudPlayer?.xp ?: 0L,
            cloudProgress?.tourXp ?: 0L,
            cloudLegacyProfile?.tourXp ?: 0L
        ).toInt()

        val resolvedCoins = maxOf(localCareer.coins, cloudCoins)
        val resolvedTrophies = maxOf(localCareer.trophies, cloudTrophies)
        val resolvedMulligans = maxOf(localCareer.mulligansRemaining, cloudMulligans)
        val resolvedXp = maxOf(localCareer.tourXp, cloudXp)

        val resolvedEquippedBall = if (isCloudNewer) {
            (cloudInventory?.equippedBallId ?: cloudLegacyProfile?.equippedBallId)
                ?.takeIf { it.isNotBlank() } ?: localCareer.equippedBallId
        } else {
            localCareer.equippedBallId
        }
        val resolvedEquippedTracer = if (isCloudNewer) {
            (cloudInventory?.equippedTracer ?: cloudLegacyProfile?.equippedTracer)
                ?.takeIf { it.isNotBlank() } ?: localCareer.equippedTracer
        } else {
            localCareer.equippedTracer
        }

        val resolvedCareer = localCareer.copy(
            coins = resolvedCoins,
            trophies = resolvedTrophies,
            mulligansRemaining = resolvedMulligans,
            roundsPlayed = maxOf(
                localCareer.roundsPlayed,
                (cloudStats?.roundsPlayed ?: 0L).toInt(),
                (cloudLegacyProfile?.roundsPlayed ?: 0L).toInt()
            ),
            holesPlayed = maxOf(
                localCareer.holesPlayed,
                (cloudStats?.holesPlayed ?: 0L).toInt(),
                (cloudLegacyProfile?.holesPlayed ?: 0L).toInt()
            ),
            birdies = maxOf(
                localCareer.birdies,
                (cloudStats?.birdies ?: 0L).toInt(),
                (cloudLegacyProfile?.birdies ?: 0L).toInt()
            ),
            eagles = maxOf(
                localCareer.eagles,
                (cloudStats?.eagles ?: 0L).toInt(),
                (cloudLegacyProfile?.eagles ?: 0L).toInt()
            ),
            aces = maxOf(
                localCareer.aces,
                (cloudStats?.aces ?: 0L).toInt(),
                (cloudLegacyProfile?.aces ?: 0L).toInt()
            ),
            longestDriveYds = maxOf(
                localCareer.longestDriveYds,
                (cloudStats?.longestDriveYds ?: 0.0).toFloat(),
                (cloudLegacyProfile?.longestDriveYds ?: 0.0).toFloat()
            ),
            greensInRegulationCount = maxOf(
                localCareer.greensInRegulationCount,
                (cloudStats?.greensInRegulationCount ?: 0L).toInt()
            ),
            tourXp = resolvedXp,
            shootoutHighScore = maxOf(
                localCareer.shootoutHighScore,
                (cloudStats?.shootoutHighScore ?: 0L).toInt(),
                (cloudLegacyProfile?.shootoutHighScore ?: 0L).toInt()
            ),
            equippedBallId = resolvedEquippedBall,
            equippedTracer = resolvedEquippedTracer,
            matchPlayWins = maxOf(
                localCareer.matchPlayWins,
                (cloudStats?.matchPlayWins ?: 0L).toInt()
            ),
            matchPlayLosses = maxOf(
                localCareer.matchPlayLosses,
                (cloudStats?.matchPlayLosses ?: 0L).toInt()
            )
        )

        val unlockedBallSet = buildSet {
            add("standard_white")
            localBalls.filter { it.isUnlocked }.forEach { add(it.id) }
            cloudInventory?.unlockedBallIds?.forEach { add(it) }
            cloudLegacyProfile?.unlockedBallIds?.forEach { add(it) }
        }
        val resolvedBalls = localBalls.map { ball ->
            val unlocked = unlockedBallSet.contains(ball.id)
            ball.copy(
                isUnlocked = unlocked,
                isEquipped = ball.id == resolvedCareer.equippedBallId && unlocked
            )
        }

        val cloudClubLevels = mapOf(
            ClubType.DRIVER to maxOf(cloudInventory?.driverLevel ?: 1L, cloudLegacyProfile?.driverLevel ?: 1L).toInt(),
            ClubType.WOOD_3 to maxOf(cloudInventory?.wood3Level ?: 1L, cloudLegacyProfile?.wood3Level ?: 1L).toInt(),
            ClubType.IRON_4 to (cloudInventory?.iron4Level ?: 1L).toInt(),
            ClubType.IRON_7 to maxOf(cloudInventory?.iron7Level ?: 1L, cloudLegacyProfile?.iron7Level ?: 1L).toInt(),
            ClubType.IRON_9 to (cloudInventory?.iron9Level ?: 1L).toInt(),
            ClubType.PITCHING_WEDGE to maxOf(cloudInventory?.wedgeLevel ?: 1L, cloudLegacyProfile?.wedgeLevel ?: 1L).toInt(),
            ClubType.SAND_WEDGE to (cloudInventory?.sandWedgeLevel ?: 1L).toInt(),
            ClubType.PUTTER to maxOf(cloudInventory?.putterLevel ?: 1L, cloudLegacyProfile?.putterLevel ?: 1L).toInt()
        )
        val resolvedClubs = localClubs.map { club ->
            val targetLevel = maxOf(club.level, (cloudClubLevels[club.clubType] ?: 1).coerceIn(1, club.maxLevel))
            val diff = (targetLevel - club.level).coerceAtLeast(0)
            if (diff > 0) {
                club.copy(
                    level = targetLevel,
                    powerStat = club.powerStat + diff * 6,
                    controlStat = club.controlStat + diff * 5,
                    spinStat = club.spinStat + diff * 5
                )
            } else {
                club
            }
        }

        val mergedFreeTiers = (localTourPass.claimedFreeTiers + (cloudProgress?.claimedFreeTiers?.map { it.toInt() } ?: emptyList())).distinct()
        val mergedVipTiers = (localTourPass.claimedVipTiers + (cloudProgress?.claimedVipTiers?.map { it.toInt() } ?: emptyList())).distinct()
        val mergedVipUnlocked = localTourPass.isVipUnlocked || (cloudProgress?.isVipUnlocked == true)
        val resolvedTourPass = localTourPass.copy(
            currentXp = maxOf(localTourPass.currentXp, resolvedXp),
            isVipUnlocked = mergedVipUnlocked,
            claimedFreeTiers = mergedFreeTiers,
            claimedVipTiers = mergedVipTiers,
            tiers = localTourPass.tiers.map { tier ->
                tier.copy(
                    isFreeClaimed = tier.isFreeClaimed || mergedFreeTiers.contains(tier.tierLevel),
                    isVipClaimed = tier.isVipClaimed || mergedVipTiers.contains(tier.tierLevel)
                )
            }
        )

        val resolvedGolfer = if (isCloudNewer) {
            val cloudName = cloudPlayer?.username?.takeIf { it.isNotBlank() }
                ?: cloudLegacyProfile?.golferName?.takeIf { it.isNotBlank() }
                ?: localGolfer.golferName
            val cloudCountry = cloudPlayer?.countryTag?.takeIf { it.isNotBlank() } ?: localGolfer.countryTag
            val stanceEnum = cloudInventory?.stance?.let { name -> GolferStance.entries.firstOrNull { it.name == name } } ?: localGolfer.stance
            val swingEnum = cloudInventory?.swingStyle?.let { name -> GolferSwingStyle.entries.firstOrNull { it.name == name } } ?: localGolfer.swingStyle
            localGolfer.copy(
                golferName = cloudName,
                countryTag = cloudCountry,
                stance = stanceEnum,
                swingStyle = swingEnum,
                visorColorHex = cloudInventory?.visorColorHex ?: localGolfer.visorColorHex,
                poloColorHex = cloudInventory?.poloColorHex ?: localGolfer.poloColorHex,
                gloveStyle = cloudInventory?.gloveStyle?.takeIf { it.isNotBlank() } ?: localGolfer.gloveStyle
            )
        } else {
            localGolfer
        }

        val resolvedSettings = if (isCloudNewer && cloudSettings != null) {
            val gTier = GraphicsQualityTier.entries.firstOrNull { it.name == cloudSettings.graphicsTier } ?: localSettings.graphicsTier
            val aiTier = AiDifficultyTier.entries.firstOrNull { it.name == cloudSettings.aiDifficulty } ?: localSettings.aiDifficulty
            localSettings.copy(
                isSoundEnabled = cloudSettings.isSoundEnabled,
                soundVolume = cloudSettings.soundVolume.toFloat().coerceIn(0f, 1f),
                isHapticsEnabled = cloudSettings.isHapticsEnabled,
                isCaddieAudioTipsEnabled = cloudSettings.isCaddieAudioTipsEnabled,
                isHighFpsMode = cloudSettings.isHighFpsMode,
                isPuttingGridVisible = cloudSettings.isPuttingGridVisible,
                isSlopeBreakRadarVisible = cloudSettings.isSlopeBreakRadarVisible,
                isCloudSyncEnabled = cloudSettings.isCloudSyncEnabled,
                graphicsTier = gTier,
                aiDifficulty = aiTier
            )
        } else {
            localSettings
        }

        val mergedCourses = (localUnlockedCourses + (cloudProgress?.unlockedCourses ?: emptyList())).distinct()
        val mergedAchievements = localUnlockedAchievements + (cloudProgress?.unlockedAchievementIds?.toSet() ?: emptySet())
        val mergedCompletedChallenges = localCompletedChallenges + (cloudProgress?.completedChallengeIds?.toSet() ?: emptySet())
        val mergedClaimedChallenges = localClaimedChallenges + (cloudProgress?.claimedChallengeIds?.toSet() ?: emptySet())
        val nextSyncRevision = (cloudProgress?.syncRevision ?: 0L) + 1L

        return ResolvedCloudSyncState(
            career = resolvedCareer,
            balls = resolvedBalls,
            clubs = resolvedClubs,
            tourPassState = resolvedTourPass,
            golferProfile = resolvedGolfer,
            playSettings = resolvedSettings,
            unlockedCourseIds = mergedCourses,
            unlockedAchievementIds = mergedAchievements,
            completedChallengeIds = mergedCompletedChallenges,
            claimedChallengeIds = mergedClaimedChallenges,
            syncRevision = nextSyncRevision
        )
    }

    // =========================================================================
    // SECURE GAME SERVICES BACKEND OPERATIONS (Anti-Cheat, Audit Logs, Rate Limits & Verified Tournament Registration)
    // =========================================================================

    suspend fun checkAndRecordRateLimit(actionName: String): Result<CloudRateLimitRecord> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in for rate-limited operations" }
        val docRef = db.collection("rate_limits").document(uid)
        val nowMs = System.currentTimeMillis()
        try {
            val snap = docRef.get().await()
            val existing = if (snap.exists()) {
                snap.toObject(CloudRateLimitRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null

            val isSameWindow = existing != null && (nowMs - existing.windowStartEpochMs) in 0L..60_000L
            val nextCount = if (isSameWindow) (existing!!.requestCount + 1L) else 1L
            require(nextCount <= 60L) { "Rate limit exceeded (max 60 requests/min)" }

            val record = CloudRateLimitRecord(
                id = uid,
                userId = uid,
                windowStartEpochMs = if (isSameWindow) existing!!.windowStartEpochMs else nowMs,
                requestCount = nextCount,
                lastAction = actionName.take(64),
                updatedAt = null
            )
            docRef.set(record).await()
            record
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            throw e
        }
    }

    suspend fun appendAuditLog(
        actionType: String,
        resourceType: String,
        resourceId: String,
        status: String,
        metadataSummary: String,
        targetUserId: String? = null
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to write audit logs" }
        val logId = "audit_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("audit_logs").document(logId)
        val record = CloudAuditLogRecord(
            id = logId,
            actorId = uid,
            targetUserId = (targetUserId ?: uid).take(128),
            actionType = actionType.take(64),
            resourceType = resourceType.take(64),
            resourceId = resourceId.take(128),
            status = status.take(16),
            metadataSummary = metadataSummary.take(300),
            createdAt = null
        )
        try {
            docRef.set(record).await()
            logId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun reportAntiCheatFlag(
        matchId: String,
        reasonCode: String,
        severity: String,
        details: String,
        reportedStrokes: Int,
        reportedDriveYds: Float,
        roundDurationSec: Int,
        tournamentId: String? = null
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to report telemetry flags" }
        val flagId = "flag_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("anti_cheat_flags").document(flagId)
        val flag = CloudAntiCheatFlag(
            id = flagId,
            userId = uid,
            matchId = matchId.take(128),
            tournamentId = tournamentId?.take(64),
            reasonCode = reasonCode.take(64),
            severity = severity.take(16),
            details = details.take(300),
            reportedStrokes = reportedStrokes.toLong().coerceAtLeast(0L),
            reportedDriveYds = reportedDriveYds.toDouble().coerceAtLeast(0.0),
            roundDurationSec = roundDurationSec.toLong().coerceAtLeast(0L),
            reviewStatus = "PENDING_REVIEW",
            reviewedBy = null,
            reviewNotes = null,
            createdAt = null,
            updatedAt = null
        )
        try {
            docRef.set(flag).await()
            appendAuditLog(
                actionType = "SCORE_SUBMITTED",
                resourceType = "anti_cheat_flags",
                resourceId = flagId,
                status = "FLAGGED",
                metadataSummary = "Anti-cheat telemetry flag: $reasonCode ($details)"
            )
            flagId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun registerForTournamentVerified(
        tournamentId: String,
        username: String,
        entryFeePaid: Int,
        trophiesAtEntry: Int,
        idempotencyKey: String = "${tournamentId}_${UUID.randomUUID().toString().take(8)}"
    ): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to register for tournament" }
        checkAndRecordRateLimit("TOURNAMENT_REGISTER").getOrThrow()
        val safeTourId = tournamentId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
        val regId = "${safeTourId}_${uid}".take(120)
        val docRef = db.collection("tournament_registrations").document(regId)

        val existing = docRef.get().await()
        if (existing.exists()) {
            throw IllegalStateException("ALREADY_EXISTS: Player is already registered for $safeTourId")
        }

        val registration = CloudTournamentRegistration(
            id = regId,
            userId = uid,
            tournamentId = safeTourId,
            username = username.ifBlank { "Apex Pro" }.take(80),
            entryFeePaid = entryFeePaid.toLong().coerceIn(0L, 100000L),
            trophiesAtEntry = trophiesAtEntry.toLong().coerceAtLeast(0L),
            status = "REGISTERED",
            idempotencyKey = idempotencyKey.take(128),
            registeredAt = null,
            updatedAt = null
        )

        try {
            docRef.set(registration).await()
            if (entryFeePaid > 0) {
                recordRewardTransaction(
                    rewardType = "TOURNAMENT_ENTRY_FEE",
                    sourceId = safeTourId,
                    coinsDelta = -entryFeePaid,
                    xpDelta = 0
                ).getOrThrow()
            }
            appendAuditLog(
                actionType = "TOURNAMENT_REGISTERED",
                resourceType = "tournament_registrations",
                resourceId = regId,
                status = "SUCCESS",
                metadataSummary = "Registered for $safeTourId (fee=$entryFeePaid)"
            ).getOrThrow()
            regId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun submitVerifiedMatchScore(
        mode: String,
        courseName: String,
        holesPlayed: Int,
        totalStrokes: Int,
        scoreToPar: Int,
        maxDriveYds: Float,
        roundDurationSec: Int,
        idempotencyKey: String
    ): Result<VerifiedScoreSubmissionResult> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in to submit verified score" }
        checkAndRecordRateLimit("SUBMIT_MATCH_SCORE").getOrThrow()

        val safeKey = idempotencyKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(56)
        val matchId = "match_${uid}_${safeKey}".take(120)
        val matchRef = db.collection("match_history").document(matchId)

        // 1. Reject duplicate score submissions
        if (matchRef.get().await().exists()) {
            appendAuditLog(
                actionType = "SCORE_SUBMITTED",
                resourceType = "match_history",
                resourceId = matchId,
                status = "REJECTED",
                metadataSummary = "Duplicate score submission rejected for $matchId"
            )
            throw IllegalStateException("ALREADY_EXISTS: Duplicate score submission rejected ($matchId)")
        }

        // 2. Anti-cheat physics & timing validation
        if (totalStrokes < holesPlayed || maxDriveYds > 430f || roundDurationSec < holesPlayed * 8) {
            val reason = when {
                totalStrokes < holesPlayed -> "IMPOSSIBLE_STROKES"
                maxDriveYds > 430f -> "DRIVE_SPEED_EXCEEDED"
                else -> "DURATION_ANOMALY"
            }
            val flagId = reportAntiCheatFlag(
                matchId = matchId,
                reasonCode = reason,
                severity = "CRITICAL",
                details = "strokes=$totalStrokes, holes=$holesPlayed, maxDrive=$maxDriveYds, duration=${roundDurationSec}s",
                reportedStrokes = totalStrokes,
                reportedDriveYds = maxDriveYds,
                roundDurationSec = roundDurationSec
            ).getOrThrow()
            throw IllegalStateException("ANTI_CHEAT_REJECTED ($reason): Flagged as $flagId")
        }

        // 3. Server-grade reward calculation
        val underParBonus = if (scoreToPar < 0) kotlin.math.abs(scoreToPar) * 50 else 0
        val coinsEarned = (holesPlayed * 60 + underParBonus).coerceIn(25, 50000)
        val xpEarned = (holesPlayed * 80 + if (scoreToPar < 0) kotlin.math.abs(scoreToPar) * 60 else 0).coerceIn(50, 50000)
        val trophiesDelta = if (scoreToPar <= 0) kotlin.math.abs(scoreToPar) * 2 + 2 else 1
        val banner = when {
            holesPlayed == 1 && totalStrokes == 1 -> "HOLE IN ONE!"
            scoreToPar <= -2 -> "EAGLE! $scoreToPar"
            scoreToPar == -1 -> "BIRDIE! -1"
            scoreToPar == 0 -> "EVEN PAR"
            else -> "+$scoreToPar"
        }

        recordMatchHistory(
            mode = mode,
            courseName = courseName,
            holesPlayed = holesPlayed,
            totalStrokes = totalStrokes,
            scoreToPar = scoreToPar,
            scoreBanner = banner,
            coinsEarned = coinsEarned,
            xpEarned = xpEarned,
            matchId = matchId
        ).getOrThrow()

        recordRewardTransaction(
            rewardType = "HOLE_COMPLETION",
            sourceId = matchId.take(80),
            coinsDelta = coinsEarned,
            xpDelta = xpEarned,
            txId = "tx_${matchId}".take(120)
        ).getOrThrow()

        appendAuditLog(
            actionType = "SCORE_SUBMITTED",
            resourceType = "match_history",
            resourceId = matchId,
            status = "SUCCESS",
            metadataSummary = "Verified $mode on $courseName: strokes=$totalStrokes ($scoreToPar), +$coinsEarned coins"
        ).getOrThrow()

        VerifiedScoreSubmissionResult(
            matchId = matchId,
            verificationStatus = "VERIFIED",
            scoreBanner = banner,
            coinsEarned = coinsEarned,
            xpEarned = xpEarned,
            trophiesDelta = trophiesDelta
        )
    }

    /**
     * Google Play Account Deletion Policy compliance:
     * Permanently deletes all owner-owned player profile, statistics, inventory, progression,
     * game settings, leaderboard, achievement, daily challenge, and match history records in Firestore.
     */
    suspend fun deleteAllUserData(userId: String): Result<Int> = runCatching {
        val activeUid = auth.currentUser?.uid
        require(activeUid != null && activeUid == userId) {
            "Authenticated owner session required to delete user cloud data"
        }

        val refsToDelete = mutableListOf(
            db.collection("players").document(userId),
            db.collection("player_statistics").document(userId),
            db.collection("player_inventory").document(userId),
            db.collection("player_progress").document(userId),
            db.collection("game_settings").document(userId),
            db.collection("users").document(userId),
            db.collection("users").document(userId).collection("tourPass").document("season_1"),
            db.collection("leaderboards").document(userId),
            db.collection("leaderboards").document("global_trophies").collection("entries").document(userId)
        )

        val ownerScopedCollections = listOf("match_history", "achievements", "daily_challenges")
        for (collectionName in ownerScopedCollections) {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.forEach { doc -> refsToDelete.add(doc.reference) }
        }

        var deletedCount = 0
        for (chunk in refsToDelete.chunked(400)) {
            val batch = db.batch()
            chunk.forEach { ref -> batch.delete(ref) }
            batch.commit().await()
            deletedCount += chunk.size
        }

        Log.i(TAG, "Deleted $deletedCount Firestore documents for user $userId")
        deletedCount
    }.onFailure { error ->
        if (error is CancellationException) throw error
        Log.e(TAG, "Failed to delete cloud user data for $userId: ${error.message}", error)
    }

    companion object {
        private const val TAG = "GolfCloudRepository"
    }
}
