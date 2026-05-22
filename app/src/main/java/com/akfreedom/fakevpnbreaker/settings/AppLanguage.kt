package com.akfreedom.fakevpnbreaker.settings

enum class AppLanguage(val storageValue: String, val localeTag: String) {
    English("en", "en"),
    Russian("ru", "ru");

    companion object {
        val Default = English

        fun fromStorageValue(value: String?): AppLanguage =
            entries.firstOrNull { it.storageValue == value } ?: Default
    }
}
