package com.emretenoz.standby.ui.standby

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmCountdownTest {
    @Test
    fun `countdown splits remaining time into hours minutes and seconds`() {
        assertEquals(
            AlarmCountdownParts(hours = 2, minutes = 3, seconds = 4),
            alarmCountdownParts(triggerAtMillis = 7_384_000L, nowMillis = 0L),
        )
    }

    @Test
    fun `countdown never becomes negative`() {
        assertEquals(
            AlarmCountdownParts(hours = 0, minutes = 0, seconds = 0),
            alarmCountdownParts(triggerAtMillis = 1_000L, nowMillis = 2_000L),
        )
    }
}
