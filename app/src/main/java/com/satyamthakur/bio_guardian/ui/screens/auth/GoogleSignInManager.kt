package com.satyamthakur.bio_guardian.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.satyamthakur.bio_guardian.R
import kotlinx.coroutines.tasks.await

data class UserInfo(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)

class GoogleSignInManager(private val context: Context) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // You'll need to add this to strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Check if user is already signed in
    fun getCurrentUser(): UserInfo? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.let {
            UserInfo(
                uid = it.id ?: "",
                displayName = it.displayName,
                email = it.email,
                photoUrl = it.photoUrl?.toString()
            )
        }
    }

    // Sign in with One Tap
    suspend fun beginSignIn(): IntentSenderRequest? {
        return try {
            val result = oneTapClient.beginSignIn(buildSignInRequest()).await()
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "One Tap sign-in failed", e)
            null
        }
    }

    // Fallback to traditional Google Sign-In
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Handle One Tap result
    suspend fun handleOneTapResult(data: Intent): UserInfo? {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            UserInfo(
                uid = credential.id,
                displayName = credential.displayName,
                email = credential.id, // One Tap uses email as ID
                photoUrl = credential.profilePictureUri?.toString()
            )
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Failed to handle One Tap result", e)
            null
        }
    }

    // Handle traditional Google Sign-In result
    suspend fun handleSignInResult(data: Intent): UserInfo? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()
            UserInfo(
                uid = account.id ?: "",
                displayName = account.displayName,
                email = account.email,
                photoUrl = account.photoUrl?.toString()
            )
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Traditional sign-in failed", e)
            null
        }
    }

    // Sign out
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            oneTapClient.signOut().await()
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Sign out failed", e)
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}