package com.emretenoz.standby.ui.standby

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.emretenoz.standby.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emretenoz.standby.data.settings.StandBySettings
import com.emretenoz.standby.data.settings.WidgetColumn
import com.emretenoz.standby.system.ChargingState
import com.emretenoz.standby.system.NextAlarmState
import com.emretenoz.standby.data.weather.WeatherState
import com.emretenoz.standby.data.media.MediaState
import com.emretenoz.standby.ui.StandByUiState
import kotlinx.coroutines.delay

@Composable
fun StandByRoot(
    state: StandByUiState,
    actions: StandByActions,
    settingsContent: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = state.isStandByActive,
        transitionSpec = { fadeIn(tween(420)) togetherWith fadeOut(tween(260)) },
        label = "standby-mode",
    ) { active ->
        if (active) {
            StandByDisplay(state.settings, state.charging, state.nextAlarm, state.weather, state.media, actions)
        } else {
            settingsContent()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandByDisplay(
    settings: StandBySettings,
    charging: ChargingState,
    nextAlarm: NextAlarmState = NextAlarmState(),
    weather: WeatherState = WeatherState(),
    media: MediaState = MediaState(),
    actions: StandByActions = StandByActions(),
) {
    StandByThemeProvider(settings) {
        ThemedBackground(
            settings = settings,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            StandByDisplayContent(settings, charging, nextAlarm, weather, media, actions)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StandByDisplayContent(
    settings: StandBySettings,
    charging: ChargingState,
    nextAlarm: NextAlarmState,
    weather: WeatherState,
    media: MediaState,
    actions: StandByActions,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    var editingStack by remember { mutableStateOf<WidgetColumn?>(null) }
    var editingClock by remember { mutableStateOf(false) }
    var indicatorVisible by remember { mutableStateOf(false) }
    val theme = LocalStandByTheme.current

    LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage) {
        indicatorVisible = true
        if (!pagerState.isScrollInProgress) {
            delay(1_100L)
            indicatorVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BurnInContainer(settings.burnInProtection && editingStack == null && !editingClock) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = editingStack == null && !editingClock,
            ) { page ->
                when (page) {
                    0 -> DualWidgetPage(settings, charging, nextAlarm, weather, media, actions) {
                        editingStack = it
                    }
                    1 -> FullClockPage(settings) { editingClock = true }
                    2 -> CalendarPage(nextAlarm)
                    else -> BatteryPage(settings, charging)
                }
            }
        }

        AnimatedVisibility(
            visible = indicatorVisible && editingStack == null && !editingClock,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(380)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(4) { index ->
                    Box(
                        Modifier
                            .size(if (index == pagerState.currentPage) 6.dp else 4.dp)
                            .alpha(if (index == pagerState.currentPage) 0.7f else 0.24f)
                            .background(theme.accent, CircleShape)
                    )
                }
            }
        }

        actions.onExitPreview?.let { exit ->
            val exitPreviewLabel = stringResource(R.string.exit_preview)
            Text(
                text = "×",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .minimumInteractiveComponentSize()
                    .semantics { contentDescription = exitPreviewLabel }
                    .clickable(role = Role.Button, onClick = exit)
                    .padding(14.dp),
                color = theme.secondary.copy(alpha = 0.55f),
                fontSize = 26.sp,
            )
        }

        AnimatedVisibility(
            visible = editingStack != null,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180)),
        ) {
            editingStack?.let { column ->
                StackEditor(
                    selectedColumn = column,
                    settings = settings,
                    onToggleWidget = actions.onToggleWidget ?: { _, _ -> },
                    onMoveWidget = actions.onMoveWidget ?: { _, _, _ -> },
                    onClockStyleChanged = actions.onClockWidgetStyleChanged ?: {},
                    onDateStyleChanged = actions.onDateWidgetStyleChanged ?: {},
                    onBatteryStyleChanged = actions.onBatteryWidgetStyleChanged ?: {},
                    onSelectColumn = { editingStack = it },
                    onDone = { editingStack = null },
                )
            }
        }

        AnimatedVisibility(
            visible = editingClock,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180)),
        ) {
            ClockCustomizer(
                settings = settings,
                actions = actions,
                onDone = { editingClock = false },
            )
        }
    }
}
