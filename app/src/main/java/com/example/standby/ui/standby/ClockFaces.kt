package com.example.standby.ui.standby

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.standby.data.settings.ClockFace
import com.example.standby.data.settings.StandBySettings
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
fun FullClockPage(settings: StandBySettings, onEditRequest: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditRequest()
                },
            ),
    ) {
        ClockFaceContent(settings, Modifier.fillMaxSize())
    }
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
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val size = min(maxHeight.value * 0.48f, maxWidth.value * 0.205f).sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(now, settings),
                modifier = Modifier.fillMaxWidth(),
                color = settings.accentColor(),
                fontSize = size,
                lineHeight = size,
                fontWeight = FontWeight.Thin,
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
                SplitNumber(formatHour(now, settings), size.value.toInt(), settings)
                Text(
                    text = "·",
                    color = settings.secondaryColor(),
                    fontSize = (size.value * 0.7f).sp,
                    fontWeight = FontWeight.Thin,
                )
                SplitNumber(now.format(DateTimeFormatter.ofPattern("mm")), size.value.toInt(), settings)
                if (settings.showSeconds) {
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("ss")),
                        color = settings.secondaryColor(),
                        fontSize = (size.value * 0.32f).sp,
                        style = tabularStyle,
                    )
                }
            }
            ClockDate(settings, now)
        }
    }
}

@Composable
private fun SplitNumber(value: String, size: Int, settings: StandBySettings) {
    Text(
        text = value,
        color = settings.accentColor(),
        fontSize = size.sp,
        lineHeight = size.sp,
        fontWeight = FontWeight.Light,
        style = tabularStyle,
    )
}

@Composable
private fun AnalogClockFace(settings: StandBySettings, modifier: Modifier) {
    val now = rememberStandByTime(settings.showSeconds, smooth = settings.showSeconds)
    val accent = settings.accentColor()
    val secondary = settings.secondaryColor()
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
                drawHand(center, radius * 0.82f, second * 6f, secondary, 1.5.dp.toPx())
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
    val accent = settings.accentColor()
    val secondary = settings.secondaryColor()
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
    val utc = local.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    Row(
        modifier = modifier.padding(horizontal = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WorldTimeBlock("LOCAL", local, settings, Alignment.Start)
        WorldTimeBlock("UTC", utc, settings, Alignment.End)
    }
}

@Composable
private fun WorldTimeBlock(
    city: String,
    time: LocalDateTime,
    settings: StandBySettings,
    alignment: Alignment.Horizontal,
) {
    Column(horizontalAlignment = alignment) {
        Text(city, color = settings.secondaryColor(), fontSize = 13.sp, letterSpacing = 2.sp)
        Text(
            formatTime(time, settings),
            color = settings.accentColor(),
            fontSize = 74.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.Thin,
            style = tabularStyle,
        )
        if (settings.showDate) {
            Text(
                time.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
                color = settings.secondaryColor(),
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
    Spacer(Modifier.height(8.dp))
    Text(
        text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
        color = settings.secondaryColor(),
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
