package com.akfreedom.fakevpnbreaker.logging

enum class EventSeverity {
    Debug,
    Info,
    Warn,
    Error,
}

data class EventLog(
    val timestampMillis: Long,
    val severity: EventSeverity,
    val message: String,
)
