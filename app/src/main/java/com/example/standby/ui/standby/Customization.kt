package com.example.standby.ui.standby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.standby.data.settings.ClockColor
import com.example.standby.data.settings.ClockFace
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn

@Composable
fun StackEditor(
    selectedColumn: WidgetColumn,
    settings: StandBySettings,
    onToggleWidget: (WidgetColumn, StandByWidgetType) -> Unit,
    onMoveWidget: (WidgetColumn, Int, Int) -> Unit,
    onSelectColumn: (WidgetColumn) -> Unit,
    onDone: () -> Unit,
) {
    var addingWidget by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val selectedWidgets = if (selectedColumn == WidgetColumn.LEFT) {
        settings.leftWidgets
    } else {
        settings.rightWidgets
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.96f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StackPreview(
                title = "LEFT",
                selected = selectedColumn == WidgetColumn.LEFT,
                widgets = settings.leftWidgets,
                settings = settings,
                onClick = {
                    addingWidget = false
                    onSelectColumn(WidgetColumn.LEFT)
                },
                modifier = Modifier.weight(0.72f),
            )
            StackPreview(
                title = "RIGHT",
                selected = selectedColumn == WidgetColumn.RIGHT,
                widgets = settings.rightWidgets,
                settings = settings,
                onClick = {
                    addingWidget = false
                    onSelectColumn(WidgetColumn.RIGHT)
                },
                modifier = Modifier.weight(0.72f),
            )
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "${selectedColumn.name} STACK",
                            color = settings.secondaryColor(),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                        )
                        Text("Widgets", color = settings.accentColor(), fontSize = 26.sp)
                    }
                    TextButton(onClick = onDone) { Text("Done", color = settings.accentColor()) }
                }

                selectedWidgets.forEachIndexed { index, widget ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            widget.label,
                            modifier = Modifier.weight(1f),
                            color = settings.accentColor(),
                            fontSize = 15.sp,
                        )
                        EditorAction("↑", enabled = index > 0) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMoveWidget(selectedColumn, index, index - 1)
                        }
                        EditorAction("↓", enabled = index < selectedWidgets.lastIndex) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMoveWidget(selectedColumn, index, index + 1)
                        }
                        EditorAction("−", enabled = selectedWidgets.size > 1) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleWidget(selectedColumn, widget)
                        }
                    }
                }

                TextButton(onClick = { addingWidget = !addingWidget }) {
                    Text("＋  Add Widget", color = settings.accentColor())
                }

                AnimatedVisibility(
                    visible = addingWidget,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column {
                        StandByWidgetType.entries
                            .filterNot { it in selectedWidgets }
                            .forEach { widget ->
                                Text(
                                    text = widget.label,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onToggleWidget(selectedColumn, widget)
                                            addingWidget = false
                                        }
                                        .padding(vertical = 7.dp),
                                    color = settings.secondaryColor(),
                                    fontSize = 14.sp,
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun StackPreview(
    title: String,
    selected: Boolean,
    widgets: List<StandByWidgetType>,
    settings: StandBySettings,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .alpha(if (selected) 1f else 0.42f)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) settings.secondaryColor() else Color.Transparent,
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, color = settings.secondaryColor(), fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.size(12.dp))
        Text(
            widgets.joinToString("\n") { it.label },
            color = settings.accentColor(),
            fontSize = 13.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EditorAction(label: String, enabled: Boolean, action: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = action)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .alpha(if (enabled) 0.9f else 0.18f),
        color = Color.White,
        fontSize = 18.sp,
    )
}

@Composable
fun ClockCustomizer(
    settings: StandBySettings,
    actions: StandByActions,
    onDone: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.97f))
            .padding(horizontal = 28.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("CLOCK", color = settings.secondaryColor(), fontSize = 11.sp, letterSpacing = 2.sp)
                Text("Customize", color = settings.accentColor(), fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
            TextButton(onClick = onDone) { Text("Done", color = settings.accentColor()) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ClockFace.entries.forEach { face ->
                Text(
                    text = face.label,
                    modifier = Modifier
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onClockFaceChanged?.invoke(face)
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    color = if (face == settings.clockFace) settings.accentColor()
                    else settings.secondaryColor().copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            ClockColor.entries.forEach { color ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(if (color == settings.clockColor) 34.dp else 28.dp)
                        .background(Color(color.argb), CircleShape)
                        .border(
                            2.dp,
                            if (color == settings.clockColor) Color.White else Color.Transparent,
                            CircleShape,
                        )
                        .clickable { actions.onClockColorChanged?.invoke(color) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CompactToggle("24-hour", settings.use24HourClock) {
                actions.on24HourChanged?.invoke(it)
            }
            CompactToggle("Seconds", settings.showSeconds) {
                actions.onShowSecondsChanged?.invoke(it)
            }
            CompactToggle("Date", settings.showDate) {
                actions.onShowDateChanged?.invoke(it)
            }
        }
    }
}

@Composable
private fun CompactToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White.copy(alpha = 0.68f), fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
