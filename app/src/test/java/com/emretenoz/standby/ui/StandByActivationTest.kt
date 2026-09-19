package com.emretenoz.standby.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StandByActivationTest {
    @Test
    fun previewAlwaysActivatesStandBy() {
        assertTrue(
            shouldActivateStandBy(
                automaticModeEnabled = false,
                isCharging = false,
                isLandscape = false,
                isPreviewMode = true,
            )
        )
    }

    @Test
    fun automaticModeRequiresChargingAndLandscape() {
        assertTrue(shouldActivateStandBy(true, true, true, false))
        assertFalse(shouldActivateStandBy(true, false, true, false))
        assertFalse(shouldActivateStandBy(true, true, false, false))
        assertFalse(shouldActivateStandBy(false, true, true, false))
    }
}
