package com.emretenoz.standby.data.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeTest {
    @Test
    fun `WMO weather codes map to display groups`() {
        assertEquals(WeatherKind.CLEAR, weatherKindForCode(0))
        assertEquals(WeatherKind.PARTLY_CLOUDY, weatherKindForCode(2))
        assertEquals(WeatherKind.FOG, weatherKindForCode(45))
        assertEquals(WeatherKind.RAIN, weatherKindForCode(63))
        assertEquals(WeatherKind.SNOW, weatherKindForCode(85))
        assertEquals(WeatherKind.THUNDERSTORM, weatherKindForCode(96))
        assertEquals(WeatherKind.CLOUDY, weatherKindForCode(500))
    }
}
