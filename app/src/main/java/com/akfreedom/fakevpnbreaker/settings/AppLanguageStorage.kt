package com.akfreedom.fakevpnbreaker.settings

import android.content.Context

object AppLanguageStorage {
    private const val PREFERENCES_NAME = "fake_vpn_breaker_settings"
    private const val KEY_APP_LANGUAGE = "app_language"

    fun get(context: Context): AppLanguage {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromStorageValue(preferences.getString(KEY_APP_LANGUAGE, null))
    }

    fun set(context: Context, language: AppLanguage) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(KEY_APP_LANGUAGE, language.storageValue).apply()
    }
}
