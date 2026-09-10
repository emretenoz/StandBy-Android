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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
    onPreview: () -> Unit,
    onOpenScreenSaverSettings: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text("StandBy", fontSize = 34.sp, fontWeight = FontWeight.Light)
            Text(
                text = statusText(state),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
            )
            Text(
                text = "Charging detected: ${if (state.charging.isCharging) "YES" else "NO"}  •  " +
                    "Battery: ${state.charging.batteryPercent}%",
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Text(
                text = "Landscape detected: ${if (state.isLandscape) "YES" else "NO"}  •  " +
                    "Source: ${state.charging.source.name}",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Button(
                onClick = onPreview,
                modifier = Modifier.padding(top = 18.dp),
            ) {
                Text("Test StandBy screen")
            }
            Button(
                onClick = onOpenScreenSaverSettings,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Open Android screen saver settings")
            }
            Spacer(Modifier.height(28.dp))
            SettingSwitch(
                title = "Automatic StandBy",
                description = "Show StandBy while this app is open, charging, and in landscape.",
                checked = state.settings.automaticModeEnabled,
                onCheckedChange = onAutomaticModeChanged,
            )
            SettingSwitch("24-hour clock", null, state.settings.use24HourClock, on24HourChanged)
            SettingSwitch("Show seconds", null, state.settings.showSeconds, onShowSecondsChanged)
            SettingSwitch(
                title = "Keep screen awake",
                description = "Overrides the timeout choice while StandBy is active.",
                checked = state.settings.keepScreenAwake,
                onCheckedChange = onKeepAwakeChanged,
            )
            TimeoutSetting(
                selected = state.settings.screenTimeout,
                enabled = !state.settings.keepScreenAwake,
                onSelected = onScreenTimeoutChanged,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Swipe horizontally between the clock, date, and charging pages once StandBy activates.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
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
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp)
            if (description != null) {
                Text(
                    description,
                    modifier = Modifier.padding(top = 3.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun TimeoutSetting(
    selected: ScreenTimeout,
    enabled: Boolean,
    onSelected: (ScreenTimeout) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { expanded = true }
            .padding(vertical = 16.dp),
    ) {
        Text("Screen timeout", fontSize = 17.sp)
        Text(
            selected.label,
            modifier = Modifier.padding(top = 3.dp),
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            fontSize = 13.sp,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ScreenTimeout.entries.forEach { timeout ->
                DropdownMenuItem(
                    text = { Text(timeout.label) },
                    onClick = {
                        expanded = false
                        onSelected(timeout)
                    },
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

private fun statusText(state: StandByUiState): String {
    if (!state.settings.automaticModeEnabled) return "Automatic activation is off"
    if (!state.charging.isCharging) return "Connect a charger to activate"
    if (!state.isLandscape) return "Charging — rotate to landscape"
    return when (state.charging.source) {
        ChargingSource.WIRELESS -> "Wireless charging"
        ChargingSource.WIRED -> "Wired charging"
        ChargingSource.OTHER -> "Charging"
        ChargingSource.NONE -> "Ready"
    }
}
