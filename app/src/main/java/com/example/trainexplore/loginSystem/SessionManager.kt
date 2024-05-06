package com.example.trainexplore.loginSystem


import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SessionManager {
    private const val PREF_FILE_NAME = "MeuPerfil"
    private const val USER_ID_KEY = "UserID"
    var userId: String? = null
        private set

    fun saveSessionData(userId: String, context: Context) {
        this.userId = userId

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit().putString(USER_ID_KEY, userId).apply()
    }

    fun loadSession(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        userId = sharedPreferences.getString(USER_ID_KEY, null)
    }

    fun clearSession(context: Context) {
        try {
            userId = null
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                "MeuPerfil",
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPreferences.edit().clear().apply()
        } catch (e: SecurityException) {
            Log.e("SessionManager", "Failed to clear session",e)
        }
    }

    fun getUserById(context: Context): Int {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        return sharedPreferences.getString(USER_ID_KEY, "-1")?.toIntOrNull() ?: -1
    }

    fun saveUserId(context: Context, userId: Int) {
        val editor = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(USER_ID_KEY, userId)
        editor.apply()
    }

}
