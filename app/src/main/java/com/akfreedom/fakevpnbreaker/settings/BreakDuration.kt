package com.akfreedom.fakevpnbreaker.settings

enum class BreakDuration(val millis: Long) {
    Ms300(300L),
    Ms500(500L),
    Ms1000(1000L),
    Ms2000(2000L),
    Ms5000(5000L);

    companion object {
        val Default = Ms1000

        fun fromMillis(value: Long): BreakDuration = entries.firstOrNull { it.millis == value } ?: Default

        fun fromName(value: String?): BreakDuration = entries.firstOrNull { it.name == value } ?: Default
    }
}
