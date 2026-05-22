package com.akfreedom.fakevpnbreaker.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLanguageContext {
    fun wrap(baseContext: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.localeTag)
        Locale.setDefault(locale)

        val configuration = Configuration(baseContext.resources.configuration)
        configuration.setLocale(locale)
        return baseContext.createConfigurationContext(configuration)
    }
}
