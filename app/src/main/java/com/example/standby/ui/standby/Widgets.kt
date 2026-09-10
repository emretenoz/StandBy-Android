package com.example.standby.ui.standby

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn
import com.example.standby.system.ChargingSource
import com.example.standby.system.ChargingState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

@Composable
fun DualWidgetPage(
    settings: StandBySettings,
    charging: ChargingState,
    onEditRequest: (WidgetColumn) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        WidgetStack(
            column = WidgetColumn.LEFT,
            widgets = settings.leftWidgets,
            settings = settings,
            charging = charging,
            onEditRequest = onEditRequest,
            modifier = Modifier.weight(1f),
        )
        WidgetStack(
            column = WidgetColumn.RIGHT,
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
    column: WidgetColumn,
    widgets: List<StandByWidgetType>,
    settings: StandBySettings,
    charging: ChargingState,
    onEditRequest: (WidgetColumn) -> Unit,
    modifier: Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { widgets.size })
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditRequest(column)
                },
            ),
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            WidgetContent(widgets[page], settings, charging)
        }
        if (widgets.size > 1 && pagerState.isScrollInProgress) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                widgets.indices.forEach { index ->
                    Box(
                        Modifier
                            .size(if (index == pagerState.currentPage) 5.dp else 3.dp)
                            .alpha(if (index == pagerState.currentPage) 0.72f else 0.22f)
                            .then(Modifier)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(settings.accentColor())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetContent(
    widget: StandByWidgetType,
    settings: StandBySettings,
    charging: ChargingState,
) {
    when (widget) {
        StandByWidgetType.CLOCK -> CompactClockWidget(settings)
        StandByWidgetType.DATE -> CompactDateWidget(settings)
        StandByWidgetType.BATTERY -> CompactBatteryWidget(settings, charging)
        StandByWidgetType.CHARGING -> CompactChargingWidget(settings, charging)
        StandByWidgetType.WORLD_CLOCK -> CompactWorldClockWidget(settings)
    }
}

@Composable
private fun CompactClockWidget(settings: StandBySettings) {
    val now = rememberStandByTime(settings.showSeconds)
    val pattern = when {
        settings.use24HourClock && settings.showSeconds -> "HH:mm:ss"
        settings.use24HourClock -> "HH:mm"
        settings.showSeconds -> "h:mm:ss"
        else -> "h:mm"
    }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val size = min(maxHeight.value * 0.36f, maxWidth.value * 0.25f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern(pattern)),
                modifier = Modifier.fillMaxWidth(),
                color = settings.accentColor(),
                fontSize = size,
                lineHeight = size,
                fontWeight = FontWeight.Thin,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
            if (!settings.use24HourClock) {
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("a")),
                    color = settings.secondaryColor(),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

@Composable
private fun CompactDateWidget(settings: StandBySettings) {
    val now = rememberStandByTime(showSeconds = false)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())).uppercase(),
            color = settings.secondaryColor(),
            fontSize = 13.sp,
            letterSpacing = 2.5.sp,
        )
        Text(
            text = now.dayOfMonth.toString(),
            color = settings.accentColor(),
            fontSize = 88.sp,
            lineHeight = 88.sp,
            fontWeight = FontWeight.Thin,
        )
        Text(
            text = now.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())).uppercase(),
            color = settings.secondaryColor(),
            fontSize = 15.sp,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun CompactBatteryWidget(settings: StandBySettings, charging: ChargingState) {
    BatteryVisual(
        settings = settings,
        charging = charging,
        modifier = Modifier.fillMaxSize(),
        compact = true,
    )
}

@Composable
private fun CompactChargingWidget(settings: StandBySettings, charging: ChargingState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ChargingGlyph(settings, charging, 74)
        Spacer(Modifier.height(14.dp))
        Text(
            text = chargingLabel(charging).uppercase(Locale.getDefault()),
            color = settings.secondaryColor(),
            fontSize = 13.sp,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun CompactWorldClockWidget(settings: StandBySettings) {
    val local = rememberStandByTime(settings.showSeconds)
    val utc = local.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("UTC", color = settings.secondaryColor(), fontSize = 12.sp, letterSpacing = 2.sp)
        Text(
            utc.format(DateTimeFormatter.ofPattern(if (settings.use24HourClock) "HH:mm" else "h:mm")),
            color = settings.accentColor(),
            fontSize = 62.sp,
            lineHeight = 66.sp,
            fontWeight = FontWeight.Thin,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
        Text(
            utc.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
            color = settings.secondaryColor(),
            fontSize = 15.sp,
        )
    }
}

@Composable
fun CalendarPage(settings: StandBySettings) {
    val now = rememberStandByTime(showSeconds = false)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = now.dayOfMonth.toString(),
            color = settings.accentColor(),
            fontSize = 164.sp,
            lineHeight = 164.sp,
            fontWeight = FontWeight.Thin,
        )
        Column(Modifier.padding(start = 30.dp)) {
            Text(
                now.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())).uppercase(),
                color = settings.secondaryColor(),
                fontSize = 18.sp,
                letterSpacing = 3.sp,
            )
            Text(
                now.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())),
                color = settings.accentColor(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                now.year.toString(),
                color = settings.secondaryColor(),
                fontSize = 19.sp,
                letterSpacing = 2.sp,
            )
        }
    }
}

@Composable
fun BatteryPage(settings: StandBySettings, charging: ChargingState) {
    BatteryVisual(settings, charging, Modifier.fillMaxSize(), compact = false)
}

@Composable
private fun BatteryVisual(
    settings: StandBySettings,
    charging: ChargingState,
    modifier: Modifier,
    compact: Boolean,
) {
    val progress by animateFloatAsState(
        targetValue = charging.batteryPercent / 100f,
        animationSpec = tween(500),
        label = "battery-progress",
    )
    val ringSize = if (compact) 172.dp else 238.dp
    val numberSize = if (compact) 55.sp else 78.sp
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(ringSize)) {
            val stroke = if (compact) 5.dp.toPx() else 7.dp.toPx()
            drawCircle(
                color = settings.secondaryColor().copy(alpha = 0.18f),
                style = Stroke(stroke),
            )
            drawArc(
                color = settings.accentColor(),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${charging.batteryPercent}%",
                color = settings.accentColor(),
                fontSize = numberSize,
                lineHeight = numberSize,
                fontWeight = FontWeight.Thin,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
            Text(
                text = chargingLabel(charging).uppercase(Locale.getDefault()),
                color = settings.secondaryColor(),
                fontSize = if (compact) 10.sp else 13.sp,
                letterSpacing = 1.6.sp,
            )
        }
    }
}

@Composable
private fun ChargingGlyph(settings: StandBySettings, charging: ChargingState, size: Int) {
    Canvas(Modifier.size(size.dp)) {
        val color = settings.accentColor()
        val stroke = 4.dp.toPx()
        drawCircle(color.copy(alpha = 0.22f), style = Stroke(stroke))
        if (charging.isCharging) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(this@Canvas.size.width * 0.56f, this@Canvas.size.height * 0.16f)
                lineTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.51f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.43f, this@Canvas.size.height * 0.84f)
                lineTo(this@Canvas.size.width * 0.68f, this@Canvas.size.height * 0.43f)
                lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.43f)
                close()
            }
            drawPath(path, color)
        }
    }
}

fun chargingLabel(charging: ChargingState): String = when {
    charging.isCharging && charging.batteryPercent >= 100 -> "Fully charged"
    charging.source == ChargingSource.WIRELESS -> "Wireless charging"
    charging.source == ChargingSource.USB -> "USB charging"
    charging.source == ChargingSource.AC -> "AC charging"
    charging.source == ChargingSource.WIRED -> "Wired charging"
    charging.isCharging -> "Charging"
    else -> "On battery"
}
