package com.example.standby.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.standby.BuildConfig
import com.example.standby.data.settings.ClockColor
import com.example.standby.data.settings.ClockFace
import com.example.standby.data.settings.ScreenTimeout
import com.example.standby.system.ChargingSource

@Composable
fun SettingsScreen(
    state: StandByUiState,
    onAutomaticModeChanged: (Boolean) -> Unit,
    on24HourChanged: (Boolean) -> Unit,
    onShowSecondsChanged: (Boolean) -> Unit,
    onScreenTimeoutChanged: (ScreenTimeout) -> Unit,
    onKeepAwakeChanged: (Boolean) -> Unit,
    onClockFaceChanged: (ClockFace) -> Unit,
    onClockColorChanged: (ClockColor) -> Unit,
    onShowDateChanged: (Boolean) -> Unit,
    onNightModeChanged: (Boolean) -> Unit,
    onBurnInProtectionChanged: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onOpenScreenSaverSettings: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text("StandBy", fontSize = 36.sp, fontWeight = FontWeight.Light)
            Text(
                statusText(state),
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
            Button(onClick = onPreview, modifier = Modifier.padding(top = 18.dp)) {
                Text("Preview StandBy")
            }

            SettingsSection("STANDBY")
            SettingSwitch(
                "Automatic activation",
                "Activates while charging in landscape.",
                state.settings.automaticModeEnabled,
                onAutomaticModeChanged,
            )
            SettingSwitch(
                "Keep screen awake",
                "Keeps the ambient display visible while active.",
                state.settings.keepScreenAwake,
                onKeepAwakeChanged,
            )
            ChoiceSetting(
                title = "Screen timeout",
                value = state.settings.screenTimeout.label,
                enabled = !state.settings.keepScreenAwake,
                choices = ScreenTimeout.entries,
                label = { it.label },
                onSelected = onScreenTimeoutChanged,
            )

            SettingsSection("APPEARANCE")
            ChoiceSetting("Clock face", state.settings.clockFace.label,
                choices = ClockFace.entries, label = { it.label }, onSelected = onClockFaceChanged)
            ChoiceSetting("Clock color", state.settings.clockColor.label,
                choices = ClockColor.entries, label = { it.label }, onSelected = onClockColorChanged)
            SettingSwitch("24-hour time", null, state.settings.use24HourClock, on24HourChanged)
            SettingSwitch("Show seconds", null, state.settings.showSeconds, onShowSecondsChanged)
            SettingSwitch("Show date", null, state.settings.showDate, onShowDateChanged)
            SettingSwitch(
                "Night Mode",
                "Uses dim red content on a true-black background.",
                state.settings.nightMode,
                onNightModeChanged,
            )
            SettingSwitch(
                "OLED burn-in protection",
                "Subtly shifts ambient content once per minute.",
                state.settings.burnInProtection,
                onBurnInProtectionChanged,
            )

            SettingsSection("WIDGETS")
            InfoRow("Left stack", state.settings.leftWidgets.joinToString { it.label })
            InfoRow("Right stack", state.settings.rightWidgets.joinToString { it.label })
            Text(
                "Long-press either stack in StandBy to add, remove, or reorder widgets.",
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )

            SettingsSection("SYSTEM")
            TextButton(onClick = onOpenScreenSaverSettings) {
                Text("Open Android screen saver settings")
            }

            SettingsSection("ABOUT")
            InfoRow("Version", BuildConfig.VERSION_NAME)
            Text(
                "Background activity launches are restricted by Android. The system screen saver provides reliable activation after display timeout where supported by the device manufacturer.",
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        title,
        modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp)
            description?.let {
                Text(it, modifier = Modifier.padding(top = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun <T> ChoiceSetting(
    title: String,
    value: String,
    enabled: Boolean = true,
    choices: List<T>,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { expanded = true }
            .padding(vertical = 13.dp),
    ) {
        Text(title, fontSize = 16.sp)
        Text(value,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.4f),
            fontSize = 13.sp)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(label(choice)) },
                    onClick = { expanded = false; onSelected(choice) },
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 15.sp)
        Text(value, modifier = Modifier.weight(1f).padding(start = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

private fun statusText(state: StandByUiState): String = when {
    !state.settings.automaticModeEnabled -> "Automatic activation is off"
    !state.charging.isCharging -> "Connect power, then place the phone in landscape"
    !state.isLandscape -> "Charging — rotate to landscape"
    state.charging.source == ChargingSource.WIRELESS -> "Wireless charging — StandBy ready"
    else -> "Charging — StandBy ready"
}
