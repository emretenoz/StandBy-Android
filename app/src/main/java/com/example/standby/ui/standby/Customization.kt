package com.example.standby.ui.standby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.standby.data.settings.BatteryWidgetStyle
import com.example.standby.data.settings.ClockWidgetStyle
import com.example.standby.data.settings.DateWidgetStyle
import com.example.standby.data.settings.StandByThemeId
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByWidgetType
import com.example.standby.data.settings.WidgetColumn

@Composable
fun StackEditor(
    selectedColumn: WidgetColumn,
    settings: StandBySettings,
    onToggleWidget: (WidgetColumn, StandByWidgetType) -> Unit,
    onMoveWidget: (WidgetColumn, Int, Int) -> Unit,
    onClockStyleChanged: (ClockWidgetStyle) -> Unit,
    onDateStyleChanged: (DateWidgetStyle) -> Unit,
    onBatteryStyleChanged: (BatteryWidgetStyle) -> Unit,
    onSelectColumn: (WidgetColumn) -> Unit,
    onDone: () -> Unit,
) {
    var addingWidget by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val theme = LocalStandByTheme.current
    val selectedWidgets = if (selectedColumn == WidgetColumn.LEFT) {
        settings.leftWidgets
    } else {
        settings.rightWidgets
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background.copy(alpha = 0.98f)),
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
                            color = theme.secondary,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                        )
                        Text("Widgets", color = theme.primary, fontSize = 26.sp)
                    }
                    TextButton(onClick = onDone) { Text("Done", color = theme.accent) }
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
                            color = theme.primary,
                            fontSize = 15.sp,
                        )
                        when (widget) {
                            StandByWidgetType.CLOCK -> StyleAction(settings.clockWidgetStyle.label) {
                                onClockStyleChanged(settings.clockWidgetStyle.next())
                            }
                            StandByWidgetType.DATE -> StyleAction(settings.dateWidgetStyle.label) {
                                onDateStyleChanged(settings.dateWidgetStyle.next())
                            }
                            StandByWidgetType.BATTERY -> StyleAction(settings.batteryWidgetStyle.label) {
                                onBatteryStyleChanged(settings.batteryWidgetStyle.next())
                            }
                            else -> Unit
                        }
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
                    Text("＋  Add Widget", color = theme.accent)
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
                                    color = theme.secondary,
                                    fontSize = 14.sp,
                                )
                            }
                    }
                }
            }
        }
    }
}

private inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    return values[(ordinal + 1) % values.size]
}

@Composable
private fun StyleAction(label: String, onClick: () -> Unit) {
    val theme = LocalStandByTheme.current
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        color = theme.accent,
        fontSize = 10.sp,
        maxLines = 1,
    )
}

@Composable
private fun StackPreview(
    title: String,
    selected: Boolean,
    widgets: List<StandByWidgetType>,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val theme = LocalStandByTheme.current
    Column(
        modifier = modifier
            .fillMaxHeight()
            .alpha(if (selected) 1f else 0.42f)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) theme.accent.copy(alpha = 0.65f) else Color.Transparent,
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, color = theme.secondary, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.size(12.dp))
        Text(
            widgets.joinToString("\n") { it.label },
            color = theme.primary,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EditorAction(label: String, enabled: Boolean, action: () -> Unit) {
    val theme = LocalStandByTheme.current
    Text(
        text = label,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = action)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .alpha(if (enabled) 0.9f else 0.18f),
        color = theme.icon,
        fontSize = 18.sp,
    )
}

@Composable
fun ClockCustomizer(settings: StandBySettings, actions: StandByActions, onDone: () -> Unit) {
    var dayTheme by remember { mutableStateOf(settings.dayTheme) }
    var nightTheme by remember { mutableStateOf(settings.nightTheme) }
    var editingNight by remember { mutableStateOf(settings.nightMode) }
    var customBackground by remember { mutableLongStateOf(settings.customBackground) }
    var customPrimary by remember { mutableLongStateOf(settings.customPrimary) }
    var customSecondary by remember { mutableLongStateOf(settings.customSecondary) }
    var customAccent by remember { mutableLongStateOf(settings.customAccent) }
    val previewSettings = settings.copy(
        nightMode = editingNight,
        dayTheme = dayTheme,
        nightTheme = nightTheme,
        customBackground = customBackground,
        customPrimary = customPrimary,
        customSecondary = customSecondary,
        customAccent = customAccent,
    )

    StandByThemeProvider(previewSettings) {
        ClockCustomizerContent(
            settings = previewSettings,
            editingNight = editingNight,
            selectedTheme = if (editingNight) nightTheme else dayTheme,
            onEditingNightChanged = { editingNight = it },
            onThemeSelected = { selected ->
                if (editingNight) nightTheme = selected else dayTheme = selected
            },
            onCustomBackground = { customBackground = it },
            onCustomPrimary = { customPrimary = it },
            onCustomSecondary = { customSecondary = it },
            onCustomAccent = { customAccent = it },
            actions = actions,
            onDone = {
                actions.onDayThemeChanged?.invoke(dayTheme)
                actions.onNightThemeChanged?.invoke(nightTheme)
                actions.onCustomColorsChanged?.invoke(
                    customBackground, customPrimary, customSecondary, customAccent,
                )
                onDone()
            },
        )
    }
}

@Composable
private fun ClockCustomizerContent(
    settings: StandBySettings,
    editingNight: Boolean,
    selectedTheme: StandByThemeId,
    onEditingNightChanged: (Boolean) -> Unit,
    onThemeSelected: (StandByThemeId) -> Unit,
    onCustomBackground: (Long) -> Unit,
    onCustomPrimary: (Long) -> Unit,
    onCustomSecondary: (Long) -> Unit,
    onCustomAccent: (Long) -> Unit,
    actions: StandByActions,
    onDone: () -> Unit,
) {
    val theme = LocalStandByTheme.current
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background.copy(alpha = 0.985f))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CUSTOMIZE", color = theme.primary, fontSize = 25.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.width(18.dp))
                TextButton(onClick = { onEditingNightChanged(false) }) {
                    Text("Day", color = if (!editingNight) theme.accent else theme.secondary)
                }
                TextButton(onClick = { onEditingNightChanged(true) }) {
                    Text("Night", color = if (editingNight) theme.accent else theme.secondary)
                }
            }
            TextButton(onClick = onDone) { Text("Done", color = theme.accent) }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StandByThemeId.entries
                .filterNot { editingNight && it == StandByThemeId.MONO_LIGHT }
                .forEach { id ->
                    ThemePreview(
                        id = id,
                        baseSettings = settings,
                        selected = id == selectedTheme,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onThemeSelected(id)
                        },
                    )
                }
        }

        if (selectedTheme == StandByThemeId.CUSTOM) {
            CustomColorEditor(
                settings.customBackground,
                settings.customPrimary,
                settings.customSecondary,
                settings.customAccent,
                onCustomBackground,
                onCustomPrimary,
                onCustomSecondary,
                onCustomAccent,
            )
        }

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            ClockFace.entries.forEach { face ->
                Text(
                    face.label,
                    modifier = Modifier.clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        actions.onClockFaceChanged?.invoke(face)
                    }.padding(horizontal = 7.dp, vertical = 10.dp),
                    color = if (face == settings.clockFace) theme.accent else theme.secondary.copy(alpha = 0.52f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ClockColor.entries.forEach { color ->
                val swatch = if (color == ClockColor.INHERIT) theme.clockPrimary else Color(color.argb)
                Box(
                    Modifier.padding(horizontal = 7.dp).size(if (color == settings.clockColor) 31.dp else 25.dp)
                        .background(swatch, CircleShape)
                        .border(2.dp, if (color == settings.clockColor) theme.primary else Color.Transparent, CircleShape)
                        .clickable { actions.onClockColorChanged?.invoke(color) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CompactToggle("24-hour", settings.use24HourClock) { actions.on24HourChanged?.invoke(it) }
            CompactToggle("Seconds", settings.showSeconds) { actions.onShowSecondsChanged?.invoke(it) }
            CompactToggle("Date", settings.showDate) { actions.onShowDateChanged?.invoke(it) }
        }
    }
}

@Composable
private fun ThemePreview(
    id: StandByThemeId,
    baseSettings: StandBySettings,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val preview = resolveStandByTheme(baseSettings.copy(dayTheme = id, nightMode = false))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(width = 108.dp, height = 64.dp)
                .background(preview.background, RoundedCornerShape(17.dp))
                .border(2.dp, if (selected) preview.accent else preview.separator, RoundedCornerShape(17.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("12:45", color = preview.clockPrimary, fontSize = 22.sp, fontWeight = FontWeight.Light)
            Box(Modifier.align(Alignment.BottomEnd).padding(8.dp).size(5.dp).background(preview.accent, CircleShape))
        }
        Text(id.label, color = LocalStandByTheme.current.secondary, fontSize = 10.sp,
            modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun CustomColorEditor(
    background: Long,
    primary: Long,
    secondary: Long,
    accent: Long,
    onBackground: (Long) -> Unit,
    onPrimary: (Long) -> Unit,
    onSecondary: (Long) -> Unit,
    onAccent: (Long) -> Unit,
) {
    Column {
        ColorSwatches("Background", background, darkSwatches, onBackground)
        ColorSwatches("Primary", primary, lightSwatches, onPrimary)
        ColorSwatches("Secondary", secondary, mutedSwatches, onSecondary)
        ColorSwatches("Accent", accent, accentSwatches, onAccent)
    }
}

@Composable
private fun ColorSwatches(label: String, selected: Long, colors: List<Long>, onSelect: (Long) -> Unit) {
    val theme = LocalStandByTheme.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(88.dp), color = theme.secondary, fontSize = 11.sp)
        colors.forEach { argb ->
            Box(Modifier.padding(horizontal = 4.dp).size(if (argb == selected) 24.dp else 20.dp)
                .background(Color(argb), CircleShape)
                .border(1.dp, if (argb == selected) theme.primary else Color.Transparent, CircleShape)
                .clickable { onSelect(argb) })
        }
    }
}

private val darkSwatches = listOf(0xFF000000, 0xFF02050B, 0xFF100906, 0xFF000503, 0xFF171717)
private val lightSwatches = listOf(0xFFF1EDE3, 0xFFEAF5FF, 0xFFFFD7A3, 0xFFE8E4D8, 0xFFF7F7F4)
private val mutedSwatches = listOf(0xFF9D9990, 0xFF7894AE, 0xFFAA7050, 0xFF819C88, 0xFF8C78AF)
private val accentSwatches = listOf(0xFFFF9F0A, 0xFF5AC8FA, 0xFFC43C42, 0xFF3BA66B, 0xFFB06CFF)

@Composable
private fun CompactToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    val theme = LocalStandByTheme.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = theme.secondary, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
