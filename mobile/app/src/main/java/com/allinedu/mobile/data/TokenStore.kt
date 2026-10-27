package com.allinedu.mobile.data

import android.content.Context
import android.content.SharedPreferences

// Guarda el token JWT en el teléfono (SharedPreferences).
// Es el equivalente a localStorage en la web.
class TokenStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("allinedu", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_TOKEN = "allinedu_token"
    }
}