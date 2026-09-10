package com.example.standby.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn
import com.example.standby.system.ChargingSource
import com.example.standby.system.ChargingState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun StandByRoot(
    state: StandByUiState,
    onExitPreview: () -> Unit,
    onToggleWidget: (WidgetColumn, StandByWidgetType) -> Unit,
    settingsContent: @Composable () -> Unit,
) {
    AnimatedContent(
        targetState = state.isStandByActive,
        transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(300)) },
        label = "standby-mode",
    ) { active ->
        if (active) {
            StandByDisplay(
                settings = state.settings,
                charging = state.charging,
                onExitPreview = if (state.isPreviewMode) onExitPreview else null,
                onToggleWidget = onToggleWidget,
            )
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
    onExitPreview: (() -> Unit)?,
    onToggleWidget: ((WidgetColumn, StandByWidgetType) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    var isEditingWidgets by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WidgetStacksPage(settings, charging) { isEditingWidgets = true }
                1 -> ClockPage(settings)
                2 -> ClockAndDatePage(settings)
                else -> BatteryPage(charging)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .size(if (index == pagerState.currentPage) 7.dp else 5.dp)
                        .alpha(if (index == pagerState.currentPage) 0.9f else 0.35f)
                        .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                )
            }
        }

        if (onExitPreview != null) {
            Text(
                text = "EXIT PREVIEW",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable(onClick = onExitPreview)
                    .padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            )
        }

        if (isEditingWidgets) {
            WidgetEditor(
                settings = settings,
                onToggleWidget = { column, widget -> onToggleWidget?.invoke(column, widget) },
                onDone = { isEditingWidgets = false },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetStacksPage(
    settings: StandBySettings,
    charging: ChargingState,
    onEditRequest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        WidgetStack(
            widgets = settings.leftWidgets,
            settings = settings,
            charging = charging,
            onEditRequest = onEditRequest,
            modifier = Modifier.weight(1f),
        )
        WidgetStack(
            widgets = settings.rightWidgets,
            settings = settings,
            charging = charging,
            onEditRequest = onEditRequest,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetStack(
    widgets: List<StandByWidgetType>,
    settings: StandBySettings,
    charging: ChargingState,
    onEditRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberPagerState(pageCount = { widgets.size })
    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(onClick = {}, onLongClick = onEditRequest),
    ) {
        VerticalPager(state = state, modifier = Modifier.fillMaxSize()) { page ->
            StandByWidget(widgets[page], settings, charging)
        }
        if (widgets.size > 1) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                widgets.indices.forEach { index ->
                    Box(
                        Modifier
                            .size(if (index == state.currentPage) 6.dp else 4.dp)
                            .alpha(if (index == state.currentPage) 0.8f else 0.25f)
                            .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun StandByWidget(
    widget: StandByWidgetType,
    settings: StandBySettings,
    charging: ChargingState,
) {
    val now = rememberCurrentTime()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (widget) {
            StandByWidgetType.CLOCK -> ClockText(now, settings, 64)
            StandByWidgetType.DATE -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    now.format(DateTimeFormatter.ofPattern("d", Locale.getDefault())),
                    fontSize = 86.sp,
                    lineHeight = 86.sp,
                    fontWeight = FontWeight.Thin,
                )
                Text(
                    now.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())).uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp,
                )
            }
            StandByWidgetType.BATTERY -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${charging.batteryPercent}%",
                    fontSize = 72.sp,
                    lineHeight = 72.sp,
                    fontWeight = FontWeight.Thin,
                )
                Text("BATTERY", color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            }
            StandByWidgetType.CHARGING -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (charging.isCharging) "⚡" else "—", fontSize = 58.sp)
                Text(
                    chargingLabel(charging).uppercase(Locale.getDefault()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                )
            }
        }
    }
}

@Composable
private fun WidgetEditor(
    settings: StandBySettings,
    onToggleWidget: (WidgetColumn, StandByWidgetType) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("EDIT WIDGET STACKS", fontSize = 19.sp, letterSpacing = 1.sp)
            Button(onClick = onDone) { Text("Done") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            WidgetPickerColumn(
                title = "LEFT STACK",
                selected = settings.leftWidgets,
                onToggle = { onToggleWidget(WidgetColumn.LEFT, it) },
                modifier = Modifier.weight(1f),
            )
            WidgetPickerColumn(
                title = "RIGHT STACK",
                selected = settings.rightWidgets,
                onToggle = { onToggleWidget(WidgetColumn.RIGHT, it) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WidgetPickerColumn(
    title: String,
    selected: List<StandByWidgetType>,
    onToggle: (StandByWidgetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            title,
            modifier = Modifier.padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
        )
        StandByWidgetType.entries.forEach { widget ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(widget) }
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = widget in selected,
                    onCheckedChange = { onToggle(widget) },
                )
                Spacer(Modifier.width(8.dp))
                Text(widget.label, fontSize = 15.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun ClockPage(settings: StandBySettings) {
    val now = rememberCurrentTime()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ClockText(now, settings, 132)
    }
}

@Composable
private fun ClockAndDatePage(settings: StandBySettings) {
    val now = rememberCurrentTime()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClockText(now, settings, 104)
        Spacer(Modifier.height(4.dp))
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
private fun BatteryPage(charging: ChargingState) {
    val source = chargingLabel(charging)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${charging.batteryPercent}%",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 112.sp,
            lineHeight = 112.sp,
            fontWeight = FontWeight.Thin,
        )
        Text(
            text = source.uppercase(Locale.getDefault()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun ClockText(now: LocalDateTime, settings: StandBySettings, size: Int) {
    val pattern = buildString {
        append(if (settings.use24HourClock) "HH:mm" else "h:mm")
        if (settings.showSeconds) append(":ss")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = now.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault())),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = size.sp,
            lineHeight = size.sp,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        if (!settings.use24HourClock) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern("a", Locale.getDefault())),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
            )
        }
    }
}

@Composable
private fun rememberCurrentTime(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }
    return now
}

private fun chargingLabel(charging: ChargingState): String = when (charging.source) {
    ChargingSource.WIRELESS -> "Wireless charging"
    ChargingSource.WIRED -> "Wired charging"
    ChargingSource.OTHER -> "Charging"
    ChargingSource.NONE -> "Not charging"
}
