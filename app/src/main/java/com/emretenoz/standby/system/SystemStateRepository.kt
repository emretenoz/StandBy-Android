package com.emretenoz.standby.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SystemStateRepository(
    chargingObserver: ChargingObserver,
    orientationObserver: OrientationObserver,
    nextAlarmObserver: NextAlarmObserver,
    scope: CoroutineScope,
) {
    val charging: StateFlow<ChargingState> = chargingObserver.state.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        ChargingState(),
    )

    val isLandscape: StateFlow<Boolean> = orientationObserver.isLandscape.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    val nextAlarm: StateFlow<NextAlarmState> = nextAlarmObserver.state.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        NextAlarmState(),
    )
}
