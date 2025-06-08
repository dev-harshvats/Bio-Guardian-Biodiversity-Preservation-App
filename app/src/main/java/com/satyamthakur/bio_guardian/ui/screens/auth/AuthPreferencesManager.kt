package com.satyamthakur.bio_guardian.ui.screens.auth

import android.content.Context
import android.content.SharedPreferences
import com.satyamthakur.bio_guardian.ui.screens.auth.UserInfo

class AuthPreferencesManager(context: Context) {

    companion object {
        private const val PREF_NAME = "bio_guardian_auth_prefs"
        private const val KEY_IS_SIGNED_IN = "is_signed_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
        private const val KEY_SIGN_IN_TIMESTAMP = "sign_in_timestamp"

        // Session timeout in milliseconds (e.g., 30 days)
        private const val SESSION_TIMEOUT = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Save user authentication data
     */
    fun saveUserAuth(userInfo: UserInfo) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_SIGNED_IN, true)
            putString(KEY_USER_ID, userInfo.uid)
            putString(KEY_USER_NAME, userInfo.displayName ?: userInfo.email)
            putString(KEY_USER_EMAIL, userInfo.email)
            putString(KEY_USER_PHOTO_URL, userInfo.photoUrl)
            putLong(KEY_SIGN_IN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Helper function to safely get user display name
     */
    private fun getUserDisplayName(userInfo: UserInfo): String {
        return (userInfo.displayName ?: userInfo.email?.substringBefore("@")).toString()
    }

    /**
     * Get saved user authentication data
     * Returns null if user is not signed in or session has expired
     */
    fun getSavedUserAuth(): UserInfo? {
        val isSignedIn = sharedPreferences.getBoolean(KEY_IS_SIGNED_IN, false)

        if (!isSignedIn) {
            return null
        }

        // Check if session has expired
        val signInTimestamp = sharedPreferences.getLong(KEY_SIGN_IN_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime - signInTimestamp > SESSION_TIMEOUT) {
            // Session expired, clear data
            clearUserAuth()
            return null
        }

        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        val userName = sharedPreferences.getString(KEY_USER_NAME, null)
        val userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null)
        val userPhotoUrl = sharedPreferences.getString(KEY_USER_PHOTO_URL, null)

        // Validate that all required data exists
        if (userId.isNullOrBlank() || userName.isNullOrBlank() || userEmail.isNullOrBlank()) {
            clearUserAuth()
            return null
        }

        return UserInfo(
            uid = userId,
            displayName = userName, // Changed from name to displayName
            email = userEmail,
            photoUrl = userPhotoUrl
        )
    }

    /**
     * Check if user is currently signed in and session is valid
     */
    fun isUserSignedIn(): Boolean {
        return getSavedUserAuth() != null
    }

    /**
     * Clear all user authentication data
     */
    fun clearUserAuth() {
        sharedPreferences.edit().apply {
            remove(KEY_IS_SIGNED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_PHOTO_URL)
            remove(KEY_SIGN_IN_TIMESTAMP)
            apply()
        }
    }

    /**
     * Update sign-in timestamp (useful for extending session)
     */
    fun updateSignInTimestamp() {
        if (isUserSignedIn()) {
            sharedPreferences.edit().apply {
                putLong(KEY_SIGN_IN_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
        }
    }

    /**
     * Get remaining session time in milliseconds
     */
    fun getRemainingSessionTime(): Long {
        val signInTimestamp = sharedPreferences.getLong(KEY_SIGN_IN_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - signInTimestamp
        return maxOf(0, SESSION_TIMEOUT - elapsedTime)
    }

    /**
     * Check if session will expire soon (within 24 hours)
     */
    fun isSessionExpiringSoon(): Boolean {
        val remainingTime = getRemainingSessionTime()
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return remainingTime in 1 until oneDayInMillis
    }
}