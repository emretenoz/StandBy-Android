package com.emretenoz.standby.data.settings

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class WorldClockZoneTest {
    @Test
    fun everyPresetUsesAValidZoneId() {
        WorldClockZone.entries.forEach { zone ->
            assertEquals(zone.zoneId, ZoneId.of(zone.zoneId).id)
        }
    }
}
