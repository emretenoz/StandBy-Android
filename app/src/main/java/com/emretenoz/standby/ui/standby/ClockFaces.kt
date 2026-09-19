package com.emretenoz.standby.ui.standby

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.emretenoz.standby.data.settings.ClockFace
import com.emretenoz.standby.data.settings.StandBySettings
import com.emretenoz.standby.R
import com.emretenoz.standby.system.NextAlarmState
import com.emretenoz.standby.ui.localizedLabel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val tabularStyle = TextStyle(fontFeatureSettings = "tnum")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullClockPage(
    settings: StandBySettings,
    nextAlarm: NextAlarmState = NextAlarmState(),
    onEditRequest: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val customizeLabel = stringResource(R.string.clock_customize_hint)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = customizeLabel }
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditRequest()
                },
            ),
    ) {
        ClockFaceContent(settings, Modifier.fillMaxSize())
        AlarmCountdownCard(
            nextAlarm = nextAlarm,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 22.dp),
        )
    }
}

@Composable
private fun AlarmCountdownCard(nextAlarm: NextAlarmState, modifier: Modifier = Modifier) {
    val theme = LocalStandByTheme.current
    val ticker = rememberStandByTime(showSeconds = true)
    val nowMillis = ticker.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val remaining = nextAlarm.triggerAtMillis?.let { triggerAtMillis ->
        alarmCountdownParts(triggerAtMillis, nowMillis)
    }
    val countdownDescription = remaining?.let {
        stringResource(
            R.string.alarm_countdown_accessibility,
            it.hours,
            it.minutes,
            it.seconds,
        )
    } ?: stringResource(R.string.no_alarm_set)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.separator.copy(alpha = if (theme.isLight) 0.72f else 0.82f))
            .semantics { contentDescription = countdownDescription }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(theme.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(25.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.42f
                drawCircle(
                    color = theme.icon,
                    radius = radius,
                    center = center,
                    style = Stroke(2.dp.toPx()),
                )
                drawLine(
                    color = theme.icon,
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.58f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = theme.icon,
                    start = center,
                    end = Offset(center.x + radius * 0.48f, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(theme.icon, 2.dp.toPx(), center)
            }
        }
        Spacer(Modifier.width(12.dp))
        if (remaining == null) {
            Column {
                Text(
                    stringResource(R.string.alarm_countdown),
                    color = theme.secondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    stringResource(R.string.no_alarm_set),
                    color = theme.primary,
                    fontSize = 15.sp,
                )
            }
        } else {
            Column {
                Text(
                    stringResource(R.string.alarm_countdown),
                    color = theme.secondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                )
                Row(verticalAlignment = Alignment.Top) {
                    CountdownUnit(remaining.hours, R.string.countdown_hours)
                    CountdownSeparator()
                    CountdownUnit(remaining.minutes, R.string.countdown_minutes)
                    CountdownSeparator()
                    CountdownUnit(remaining.seconds, R.string.countdown_seconds)
                }
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: Long, @StringRes labelRes: Int) {
    val theme = LocalStandByTheme.current
    Column(
        modifier = Modifier.width(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = theme.clockPrimary,
            fontSize = 23.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Light,
            style = tabularStyle,
        )
        Text(
            text = stringResource(labelRes),
            color = theme.secondary,
            fontSize = 7.sp,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
private fun CountdownSeparator() {
    val theme = LocalStandByTheme.current
    Text(
        text = ":",
        modifier = Modifier.padding(top = 1.dp),
        color = theme.secondary,
        fontSize = 19.sp,
        lineHeight = 22.sp,
    )
}

internal data class AlarmCountdownParts(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
)

internal fun alarmCountdownParts(triggerAtMillis: Long, nowMillis: Long): AlarmCountdownParts {
    val totalSeconds = ((triggerAtMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1_000L
    return AlarmCountdownParts(
        hours = totalSeconds / 3_600L,
        minutes = totalSeconds / 60L % 60L,
        seconds = totalSeconds % 60L,
    )
}

@Composable
fun ClockFaceContent(settings: StandBySettings, modifier: Modifier = Modifier) {
    when (settings.clockFace) {
        ClockFace.DIGITAL -> DigitalClockFace(settings, modifier)
        ClockFace.DIGITAL_SPLIT -> SplitClockFace(settings, modifier)
        ClockFace.ANALOG -> AnalogClockFace(settings, modifier)
        ClockFace.ORBIT -> OrbitClockFace(settings, modifier)
        ClockFace.WORLD -> WorldClockFace(settings, modifier)
    }
}

@Composable
private fun DigitalClockFace(settings: StandBySettings, modifier: Modifier) {
    val now = rememberStandByTime(settings.showSeconds)
    val theme = LocalStandByTheme.current
    val typography = resolveTypography(settings, ClockFace.DIGITAL)
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val size = min(maxHeight.value * 0.48f, maxWidth.value * 0.205f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(now, settings),
                modifier = Modifier.fillMaxWidth(),
                color = theme.clockPrimary,
                fontSize = size,
                lineHeight = size,
                fontWeight = typography.weight,
                fontFamily = typography.family,
                letterSpacing = typography.letterSpacing,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = tabularStyle,
            )
            ClockDate(settings, now)
        }
    }
}

@Composable
private fun SplitClockFace(settings: StandBySettings, modifier: Modifier) {
    val now = rememberStandByTime(settings.showSeconds)
    val theme = LocalStandByTheme.current
    val typography = resolveTypography(settings, ClockFace.DIGITAL_SPLIT)
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val size = min(maxHeight.value * 0.54f, maxWidth.value * 0.22f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SplitNumber(formatHour(now, settings), size.value.toInt(), settings, theme.clockPrimary)
                Text(
                    text = "·",
                    color = theme.clockSecondary,
                    fontSize = (size.value * 0.7f).sp,
                    fontWeight = FontWeight.Thin,
                )
                SplitNumber(
                    now.format(DateTimeFormatter.ofPattern("mm")),
                    size.value.toInt(),
                    settings,
                    theme.clockSecondary,
                )
                if (settings.showSeconds) {
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("ss")),
                        color = theme.clockSecondary,
                        fontSize = (size.value * 0.32f).sp,
                        fontFamily = typography.family,
                        style = tabularStyle,
                    )
                }
            }
            ClockDate(settings, now)
        }
    }
}

@Composable
private fun SplitNumber(
    value: String,
    size: Int,
    settings: StandBySettings,
    color: androidx.compose.ui.graphics.Color,
) {
    val typography = resolveTypography(settings, ClockFace.DIGITAL_SPLIT)
    Text(
        text = value,
        modifier = Modifier.graphicsLayer(scaleX = typography.horizontalScale),
        color = color,
        fontSize = size.sp,
        lineHeight = size.sp,
        fontWeight = typography.weight,
        fontFamily = typography.family,
        letterSpacing = typography.letterSpacing,
        style = tabularStyle,
    )
}

@Composable
private fun AnalogClockFace(settings: StandBySettings, modifier: Modifier) {
    val now = rememberStandByTime(settings.showSeconds, smooth = settings.showSeconds)
    val theme = LocalStandByTheme.current
    val accent = theme.clockPrimary
    val secondary = theme.analogMarkers
    Row(
        modifier = modifier.padding(horizontal = 30.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Canvas(Modifier.size(270.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(secondary.copy(alpha = 0.4f), radius, center, style = Stroke(2.dp.toPx()))
            repeat(12) { index ->
                val angle = index * 30.0 * PI / 180.0 - PI / 2
                val outer = Offset(
                    center.x + cos(angle).toFloat() * radius * 0.9f,
                    center.y + sin(angle).toFloat() * radius * 0.9f,
                )
                drawCircle(accent.copy(alpha = if (index % 3 == 0) 0.9f else 0.38f),
                    if (index % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx(), outer)
            }
            val second = now.second + now.nano / 1_000_000_000f
            val minute = now.minute + second / 60f
            val hour = (now.hour % 12) + minute / 60f
            drawHand(center, radius * 0.48f, hour * 30f, accent, 7.dp.toPx())
            drawHand(center, radius * 0.72f, minute * 6f, accent, 4.dp.toPx())
            if (settings.showSeconds) {
                drawHand(center, radius * 0.82f, second * 6f, theme.analogSecondHand, 1.5.dp.toPx())
            }
            drawCircle(accent, 5.dp.toPx(), center)
        }
        if (settings.showDate) {
            Column(Modifier.padding(start = 34.dp)) {
                Text(
                    now.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())).uppercase(),
                    color = secondary,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    now.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())),
                    color = accent,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHand(
    center: Offset,
    length: Float,
    degrees: Float,
    color: androidx.compose.ui.graphics.Color,
    width: Float,
) {
    val angle = degrees * PI / 180.0 - PI / 2
    drawLine(
        color = color,
        start = center,
        end = Offset(
            center.x + cos(angle).toFloat() * length,
            center.y + sin(angle).toFloat() * length,
        ),
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

@Composable
private fun OrbitClockFace(settings: StandBySettings, modifier: Modifier) {
    val now = rememberStandByTime(settings.showSeconds, smooth = settings.showSeconds)
    val theme = LocalStandByTheme.current
    val accent = theme.accent
    val secondary = theme.clockSecondary
    Row(
        modifier = modifier.padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(250.dp)) {
            val radius = size.minDimension * 0.42f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(secondary.copy(alpha = 0.22f), radius, center, style = Stroke(1.dp.toPx()))
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = (now.hour % 12 + now.minute / 60f) / 12f * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(5.dp.toPx(), cap = StrokeCap.Round),
            )
            val minuteAngle = (now.minute + now.second / 60f) / 60f * 2f * PI.toFloat() - PI.toFloat() / 2f
            val dot = Offset(center.x + cos(minuteAngle) * radius, center.y + sin(minuteAngle) * radius)
            drawCircle(accent, 8.dp.toPx(), dot)
            drawCircle(accent.copy(alpha = 0.18f), radius * 0.54f, center, style = Stroke(1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                formatTime(now, settings),
                color = accent,
                fontSize = 58.sp,
                fontWeight = FontWeight.Thin,
                style = tabularStyle,
            )
            ClockDate(settings, now, Alignment.Start)
        }
    }
}

@Composable
private fun WorldClockFace(settings: StandBySettings, modifier: Modifier) {
    val local = rememberStandByTime(settings.showSeconds)
    val context = LocalContext.current
    val secondary = local.atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of(settings.worldClockZone.zoneId))
        .toLocalDateTime()
    Row(
        modifier = modifier.padding(horizontal = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WorldTimeBlock(stringResource(R.string.local_time), local, settings, Alignment.Start)
        WorldTimeBlock(
            context.localizedLabel(settings.worldClockZone).uppercase(Locale.getDefault()),
            secondary,
            settings,
            Alignment.End,
        )
    }
}

@Composable
private fun WorldTimeBlock(
    city: String,
    time: LocalDateTime,
    settings: StandBySettings,
    alignment: Alignment.Horizontal,
) {
    val theme = LocalStandByTheme.current
    val typography = resolveTypography(settings, ClockFace.WORLD)
    Column(horizontalAlignment = alignment) {
        Text(city, color = theme.secondary, fontSize = 13.sp, letterSpacing = 2.sp)
        Text(
            formatTime(time, settings),
            color = theme.clockPrimary,
            fontSize = 74.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.Thin,
            fontFamily = typography.family,
            letterSpacing = typography.letterSpacing,
            style = tabularStyle,
        )
        if (settings.showDate) {
            Text(
                time.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
                color = theme.secondary,
                fontSize = 17.sp,
            )
        }
    }
}

@Composable
private fun ClockDate(
    settings: StandBySettings,
    time: LocalDateTime,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    if (!settings.showDate) return
    val theme = LocalStandByTheme.current
    Spacer(Modifier.height(8.dp))
    Text(
        text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
        color = theme.clockSecondary,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
        fontSize = 20.sp,
        fontWeight = FontWeight.Light,
        textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start,
    )
}

private fun formatHour(time: LocalDateTime, settings: StandBySettings): String =
    time.format(DateTimeFormatter.ofPattern(if (settings.use24HourClock) "HH" else "h"))

private fun formatTime(time: LocalDateTime, settings: StandBySettings): String {
    val pattern = buildString {
        append(if (settings.use24HourClock) "HH:mm" else "h:mm")
        if (settings.showSeconds) append(":ss")
    }
    return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}
