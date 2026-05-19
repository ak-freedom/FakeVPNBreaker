package com.akfreedom.fakevpnbreaker.settings

enum class BreakDuration(val millis: Long, val label: String) {
    Ms300(300L, "300 ms"),
    Ms500(500L, "500 ms"),
    Ms1000(1000L, "1000 ms"),
    Ms2000(2000L, "2000 ms"),
    Ms5000(5000L, "5000 ms");

    companion object {
        val Default = Ms1000

        fun fromMillis(value: Long): BreakDuration = entries.firstOrNull { it.millis == value } ?: Default

        fun fromName(value: String?): BreakDuration = entries.firstOrNull { it.name == value } ?: Default
    }
}
