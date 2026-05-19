package com.akfreedom.fakevpnbreaker.logging

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EventLogRepository(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun append(severity: EventSeverity, message: String) {
        val event = EventLog(System.currentTimeMillis(), severity, message)
        writeEvents(trimToLimit(readEventsOldestFirst() + event))
        writeLogcat(severity, message)
    }

    fun getNewestFirst(): List<EventLog> = readEventsOldestFirst().asReversed()

    fun clear() {
        preferences.edit().remove(KEY_EVENTS).apply()
        append(EventSeverity.Info, "Local event log cleared")
    }

    fun formatForDisplay(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
        formatter.timeZone = TimeZone.getDefault()
        return getNewestFirst().joinToString(separator = "\n") { event ->
            "${formatter.format(Date(event.timestampMillis))} ${event.severity.name.uppercase(Locale.US)} ${event.message}"
        }.ifBlank { "No events yet." }
    }

    private fun readEventsOldestFirst(): List<EventLog> {
        val raw = preferences.getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                EventLog(
                    timestampMillis = item.getLong(JSON_TIMESTAMP),
                    severity = EventSeverity.valueOf(item.getString(JSON_SEVERITY)),
                    message = item.getString(JSON_MESSAGE),
                )
            }
        }.getOrElse {
            writeLogcat(EventSeverity.Warn, "Stored event log could not be parsed; resetting local log")
            emptyList()
        }
    }

    private fun writeEvents(events: List<EventLog>) {
        val array = JSONArray()
        events.forEach { event ->
            array.put(
                JSONObject()
                    .put(JSON_TIMESTAMP, event.timestampMillis)
                    .put(JSON_SEVERITY, event.severity.name)
                    .put(JSON_MESSAGE, event.message),
            )
        }
        preferences.edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    private fun writeLogcat(severity: EventSeverity, message: String) {
        when (severity) {
            EventSeverity.Debug -> Log.d(LOG_TAG, message)
            EventSeverity.Info -> Log.i(LOG_TAG, message)
            EventSeverity.Warn -> Log.w(LOG_TAG, message)
            EventSeverity.Error -> Log.e(LOG_TAG, message)
        }
    }

    companion object {
        const val MAX_EVENTS = 50
        private const val PREFERENCES_NAME = "fake_vpn_breaker_events"
        private const val KEY_EVENTS = "events"
        private const val LOG_TAG = "FakeVpnBreaker"
        private const val JSON_TIMESTAMP = "timestampMillis"
        private const val JSON_SEVERITY = "severity"
        private const val JSON_MESSAGE = "message"

        fun trimToLimit(events: List<EventLog>): List<EventLog> = events.takeLast(MAX_EVENTS)
    }
}
