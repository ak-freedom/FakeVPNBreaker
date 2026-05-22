package com.akfreedom.fakevpnbreaker.settings

import android.content.Context
import android.content.SharedPreferences
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import java.util.UUID

class SettingsRepository(
    private val context: Context,
    private val eventLogRepository: EventLogRepository = EventLogRepository(context),
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getBreakDuration(): BreakDuration {
        val stored = preferences.getString(KEY_BREAK_DURATION, null)
        val parsed = BreakDuration.fromName(stored)
        if (stored != null && stored != parsed.name) {
            eventLogRepository.append(EventSeverity.Warn, "Invalid hold duration preference; using ${parsed.millis}ms")
        }
        return parsed
    }

    fun setBreakDuration(duration: BreakDuration) {
        preferences.edit().putString(KEY_BREAK_DURATION, duration.name).apply()
        eventLogRepository.append(EventSeverity.Info, "Setting changed: holdDuration=${duration.millis}ms")
    }

    fun getRoutingMode(): RoutingMode {
        val stored = preferences.getString(KEY_ROUTING_MODE, null)
        val parsed = RoutingMode.fromName(stored)
        if (stored != null && stored != parsed.name) {
            eventLogRepository.append(EventSeverity.Warn, "Invalid routing mode preference; using ${parsed.name}")
        }
        return parsed
    }

    fun setRoutingMode(mode: RoutingMode) {
        preferences.edit().putString(KEY_ROUTING_MODE, mode.name).apply()
        eventLogRepository.append(EventSeverity.Info, "Setting changed: routingMode=${mode.name}")
    }

    fun shouldCloseAfterTrigger(): Boolean = preferences.getBoolean(KEY_CLOSE_AFTER_TRIGGER, true)

    fun setCloseAfterTrigger(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CLOSE_AFTER_TRIGGER, enabled).apply()
        eventLogRepository.append(EventSeverity.Info, "Setting changed: closeAfterTrigger=$enabled")
    }

    fun getAppLanguage(): AppLanguage = AppLanguageStorage.get(context)

    fun setAppLanguage(language: AppLanguage) {
        AppLanguageStorage.set(context, language)
    }

    fun getTriggerToken(): String {
        val stored = preferences.getString(KEY_TRIGGER_TOKEN, null)
        if (!stored.isNullOrBlank()) return stored

        val generated = UUID.randomUUID().toString()
        preferences.edit().putString(KEY_TRIGGER_TOKEN, generated).apply()
        eventLogRepository.append(EventSeverity.Info, "[FIX:trigger-auth] MacroDroid trigger token initialized")
        return generated
    }

    private companion object {
        const val PREFERENCES_NAME = "fake_vpn_breaker_settings"
        const val KEY_BREAK_DURATION = "break_duration"
        const val KEY_ROUTING_MODE = "routing_mode"
        const val KEY_CLOSE_AFTER_TRIGGER = "close_after_trigger"
        const val KEY_TRIGGER_TOKEN = "trigger_token"
    }
}
