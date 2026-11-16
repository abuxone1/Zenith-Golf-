package com.example.game.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Manages Firebase Authentication for Zenith Golf 2.0 using Jetpack Credential Manager & Email/Password:
 * - Interactive Google Sign-In (`GetSignInWithGoogleOption` in its own dedicated request).
 * - Email & Password Sign-In, Account Registration, and Password Recovery.
 * - Silent Account Recovery (`GetGoogleIdOption` in a separate dedicated request).
 * - Secure Sign-Out clearing both CredentialManager state and FirebaseAuth.
 * - Seamless upgrade/linking of local on-device progress (`LocalProgressStorage`) into authenticated Firebase accounts.
 */
class GoogleAuthManager(
    private val context: Context,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val credentialManager = CredentialManager.create(context)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Interactive Google Sign-In triggered by the user tapping the Sign-In button.
     */
    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleCredentialResult(result.credential)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Sign-in cancelled by user: ${e.message}")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error during sign-in: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Email & Password Sign-In via Firebase Authentication.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        val cleanEmail = email.trim()
        require(cleanEmail.isNotBlank() && cleanEmail.contains("@")) { "Please enter a valid email address." }
        require(password.length >= 6) { "Password must be at least 6 characters." }
        val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
        checkNotNull(result.user) { "Sign-in succeeded but user session was null." }
    }

    /**
     * Creates a new Firebase Email & Password account and updates display name.
     */
    suspend fun createAccountWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> = runCatching {
        val cleanEmail = email.trim()
        val cleanName = displayName.trim().ifBlank { "Tour Pro" }
        require(cleanEmail.isNotBlank() && cleanEmail.contains("@")) { "Please enter a valid email address." }
        require(password.length >= 6) { "Password must be at least 6 characters." }
        val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
        val user = checkNotNull(result.user) { "Account created but user session was null." }
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(cleanName)
            .build()
        user.updateProfile(profileUpdate).await()
        user
    }

    /**
     * Sends a password reset link to the player's registered email address.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val cleanEmail = email.trim()
        require(cleanEmail.isNotBlank() && cleanEmail.contains("@")) { "Enter your email address to reset password." }
        auth.sendPasswordResetEmail(cleanEmail).await()
        Unit
    }

    /**
     * Silent account recovery for returning players who previously authorized Zenith Golf.
     */
    suspend fun attemptSilentAccountRecovery(): Result<FirebaseUser> {
        val existingUser = auth.currentUser
        if (existingUser != null) {
            return recoverAndRefreshSession()
        }

        return try {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()

            val silentRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = silentRequest,
                context = context
            )

            handleCredentialResult(result.credential)
        } catch (e: Exception) {
            Log.d(TAG, "Silent account recovery unavailable or requires interactive sign-in: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Verifies and refreshes the active Firebase user session and ID token for account recovery.
     */
    suspend fun recoverAndRefreshSession(): Result<FirebaseUser> = runCatching {
        val user = checkNotNull(auth.currentUser) { "No active authenticated session to recover" }
        user.reload().await()
        user.getIdToken(true).await()
        checkNotNull(auth.currentUser) { "User session expired after reload" }
    }

    private suspend fun handleCredentialResult(credential: androidx.credentials.Credential): Result<FirebaseUser> {
        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign in")
            Log.i(TAG, "Successfully authenticated user: ${user.uid} (${user.email})")
            Result.success(user)
        } else {
            val errorMsg = "Unexpected credential type returned: ${credential.type}"
            Log.e(TAG, errorMsg)
            Result.failure(IllegalStateException(errorMsg))
        }
    }

    /**
     * Secure logout: clears CredentialManager state inside `NonCancellable` before signing out of FirebaseAuth.
     */
    suspend fun signOut() {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing credential state: ${e.message}", e)
            } finally {
                auth.signOut()
                Log.i(TAG, "Successfully signed out of Firebase & CredentialManager")
            }
        }
    }

    /**
     * Permanently deletes the signed-in Firebase Auth account and clears local CredentialManager state.
     */
    suspend fun deleteFirebaseAccount(): Result<Unit> = withContext(NonCancellable) {
        runCatching {
            val user = checkNotNull(auth.currentUser) { "No signed-in user account to delete" }
            user.delete().await()
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Non-fatal error clearing credential state after account deletion: ${e.message}")
            }
            Log.i(TAG, "Permanently deleted Firebase Auth user: ${user.uid}")
            Unit
        }.recoverCatching { error ->
            val msg = error.localizedMessage.orEmpty()
            if (error.javaClass.simpleName.contains("RecentLogin", ignoreCase = true) ||
                msg.contains("recent", ignoreCase = true)
            ) {
                throw IllegalStateException(
                    "For security, please sign out and sign back in before permanently deleting your account."
                )
            }
            throw error
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
    }
}
