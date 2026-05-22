package com.akfreedom.fakevpnbreaker.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun invalidStoredLanguageFallsBackToEnglish() {
        assertEquals(AppLanguage.English, AppLanguage.fromStorageValue(null))
        assertEquals(AppLanguage.English, AppLanguage.fromStorageValue(""))
        assertEquals(AppLanguage.English, AppLanguage.fromStorageValue("de"))
    }

    @Test
    fun validStoredLanguageValuesAreStable() {
        assertEquals(AppLanguage.English, AppLanguage.fromStorageValue("en"))
        assertEquals(AppLanguage.Russian, AppLanguage.fromStorageValue("ru"))
    }
}
