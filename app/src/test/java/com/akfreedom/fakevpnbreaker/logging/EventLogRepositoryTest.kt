package com.akfreedom.fakevpnbreaker.logging

import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventLogRepositoryTest {
    @Test
    fun trimToLimitKeepsLatestFiftyEvents() {
        val events = (1L..60L).map { index ->
            EventLog(index, EventSeverity.Info, "event-$index")
        }

        val trimmed = EventLogRepository.trimToLimit(events)

        assertEquals(EventLogRepository.MAX_EVENTS, trimmed.size)
        assertEquals("event-11", trimmed.first().message)
        assertEquals("event-60", trimmed.last().message)
    }

    @Test
    fun trimToLimitKeepsShortListUnchanged() {
        val events = (1L..3L).map { index ->
            EventLog(index, EventSeverity.Info, "event-$index")
        }

        val trimmed = EventLogRepository.trimToLimit(events)

        assertEquals(events, trimmed)
        assertTrue("Short event list must not be truncated", trimmed.size == 3)
    }

    @Test
    fun displayFormattingUsesLocalizedEmptyText() {
        assertEquals(
            "Событий пока нет.",
            EventLogRepository.formatForDisplay(emptyList(), "Событий пока нет.", TimeZone.getTimeZone("UTC")),
        )
    }

    @Test
    fun displayFormattingDoesNotTranslatePersistedMessages() {
        val events = listOf(EventLog(0L, EventSeverity.Info, "Dummy VPN established"))

        val formatted = EventLogRepository.formatForDisplay(events, "Событий пока нет.", TimeZone.getTimeZone("UTC"))

        assertTrue(formatted.contains("Dummy VPN established"))
    }
}
