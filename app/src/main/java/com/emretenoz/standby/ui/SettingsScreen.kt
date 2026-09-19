package com.emretenoz.standby.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emretenoz.standby.BuildConfig
import com.emretenoz.standby.R
import com.emretenoz.standby.data.settings.ClockColor
import com.emretenoz.standby.data.settings.ClockFace
import com.emretenoz.standby.data.settings.BackgroundStyle
import com.emretenoz.standby.data.settings.BatteryWidgetStyle
import com.emretenoz.standby.data.settings.ClockWidgetStyle
import com.emretenoz.standby.data.settings.DateWidgetStyle
import com.emretenoz.standby.data.settings.ScreenTimeout
import com.emretenoz.standby.data.settings.StandByThemeId
import com.emretenoz.standby.data.settings.TypographyStyle
import com.emretenoz.standby.data.settings.WorldClockZone
import com.emretenoz.standby.system.ChargingSource

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
    onDayThemeChanged: (StandByThemeId) -> Unit,
    onNightThemeChanged: (StandByThemeId) -> Unit,
    onBackgroundStyleChanged: (BackgroundStyle) -> Unit,
    onTypographyStyleChanged: (TypographyStyle) -> Unit,
    onClockWidgetStyleChanged: (ClockWidgetStyle) -> Unit,
    onDateWidgetStyleChanged: (DateWidgetStyle) -> Unit,
    onBatteryWidgetStyleChanged: (BatteryWidgetStyle) -> Unit,
    onWorldClockZoneChanged: (WorldClockZone) -> Unit,
    onWeatherEnabledChanged: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onOpenScreenSaverSettings: () -> Unit,
    onOpenMediaAccessSettings: () -> Unit,
) {
    val context = LocalContext.current
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text(stringResource(R.string.app_name), fontSize = 36.sp, fontWeight = FontWeight.Light)
            Text(
                statusText(state),
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
            Button(onClick = onPreview, modifier = Modifier.padding(top = 18.dp)) {
                Text(stringResource(R.string.preview_standby))
            }

            SettingsSection(stringResource(R.string.section_standby))
            SettingSwitch(
                stringResource(R.string.automatic_activation),
                stringResource(R.string.automatic_activation_description),
                state.settings.automaticModeEnabled,
                onAutomaticModeChanged,
            )
            SettingSwitch(
                stringResource(R.string.keep_screen_awake),
                stringResource(R.string.keep_screen_awake_description),
                state.settings.keepScreenAwake,
                onKeepAwakeChanged,
            )
            ChoiceSetting(
                title = stringResource(R.string.screen_timeout),
                value = context.localizedLabel(state.settings.screenTimeout),
                enabled = !state.settings.keepScreenAwake,
                choices = ScreenTimeout.entries,
                label = context::localizedLabel,
                onSelected = onScreenTimeoutChanged,
            )

            SettingsSection(stringResource(R.string.section_appearance))
            ChoiceSetting(stringResource(R.string.day_theme), context.localizedLabel(state.settings.dayTheme),
                choices = StandByThemeId.entries, label = context::localizedLabel, onSelected = onDayThemeChanged)
            ChoiceSetting(stringResource(R.string.night_theme), context.localizedLabel(state.settings.nightTheme),
                choices = StandByThemeId.entries.filterNot { it == StandByThemeId.MONO_LIGHT },
                label = context::localizedLabel, onSelected = onNightThemeChanged)
            ChoiceSetting(stringResource(R.string.background), context.localizedLabel(state.settings.backgroundStyle),
                choices = BackgroundStyle.entries, label = context::localizedLabel, onSelected = onBackgroundStyleChanged)
            ChoiceSetting(stringResource(R.string.clock_face), context.localizedLabel(state.settings.clockFace),
                choices = ClockFace.entries, label = context::localizedLabel, onSelected = onClockFaceChanged)
            ChoiceSetting(stringResource(R.string.clock_color), context.localizedLabel(state.settings.clockColor),
                choices = ClockColor.entries, label = context::localizedLabel, onSelected = onClockColorChanged)
            SettingSwitch(stringResource(R.string.time_24_hour), null, state.settings.use24HourClock, on24HourChanged)
            SettingSwitch(stringResource(R.string.show_seconds), null, state.settings.showSeconds, onShowSecondsChanged)
            SettingSwitch(stringResource(R.string.show_date), null, state.settings.showDate, onShowDateChanged)
            ChoiceSetting(stringResource(R.string.typography), context.localizedLabel(state.settings.typographyStyle),
                choices = TypographyStyle.entries, label = context::localizedLabel, onSelected = onTypographyStyleChanged)
            SettingSwitch(
                stringResource(R.string.night_mode),
                stringResource(R.string.night_mode_description),
                state.settings.nightMode,
                onNightModeChanged,
            )
            SettingSwitch(
                stringResource(R.string.burn_in_protection),
                stringResource(R.string.burn_in_protection_description),
                state.settings.burnInProtection,
                onBurnInProtectionChanged,
            )

            SettingsSection(stringResource(R.string.section_widgets))
            InfoRow(stringResource(R.string.left_stack), state.settings.leftWidgets.joinToString { context.localizedLabel(it) })
            InfoRow(stringResource(R.string.right_stack), state.settings.rightWidgets.joinToString { context.localizedLabel(it) })
            ChoiceSetting(stringResource(R.string.clock_widget_style), context.localizedLabel(state.settings.clockWidgetStyle),
                choices = ClockWidgetStyle.entries, label = context::localizedLabel, onSelected = onClockWidgetStyleChanged)
            ChoiceSetting(stringResource(R.string.date_widget_style), context.localizedLabel(state.settings.dateWidgetStyle),
                choices = DateWidgetStyle.entries, label = context::localizedLabel, onSelected = onDateWidgetStyleChanged)
            ChoiceSetting(stringResource(R.string.battery_widget_style), context.localizedLabel(state.settings.batteryWidgetStyle),
                choices = BatteryWidgetStyle.entries, label = context::localizedLabel, onSelected = onBatteryWidgetStyleChanged)
            ChoiceSetting(stringResource(R.string.world_clock_city), context.localizedLabel(state.settings.worldClockZone),
                choices = WorldClockZone.entries, label = context::localizedLabel, onSelected = onWorldClockZoneChanged)
            SettingSwitch(
                stringResource(R.string.weather_enabled),
                stringResource(R.string.weather_enabled_description),
                state.settings.weatherEnabled,
                onWeatherEnabledChanged,
            )
            Text(
                stringResource(R.string.widget_edit_hint),
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )

            SettingsSection(stringResource(R.string.section_system))
            TextButton(onClick = onOpenScreenSaverSettings) {
                Text(stringResource(R.string.open_screen_saver_settings))
            }
            InfoRow(
                stringResource(R.string.media_access),
                stringResource(
                    if (state.media.accessGranted) R.string.media_access_granted
                    else R.string.media_access_not_granted
                ),
            )
            Text(
                stringResource(R.string.media_access_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            TextButton(onClick = onOpenMediaAccessSettings) {
                Text(stringResource(R.string.open_media_access_settings))
            }

            SettingsSection(stringResource(R.string.section_about))
            InfoRow(stringResource(R.string.version), BuildConfig.VERSION_NAME)
            Text(
                stringResource(R.string.background_launch_explanation),
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

@Composable
private fun statusText(state: StandByUiState): String = when {
    !state.settings.automaticModeEnabled -> stringResource(R.string.status_automatic_off)
    !state.charging.isCharging -> stringResource(R.string.status_connect_power)
    !state.isLandscape -> stringResource(R.string.status_rotate_landscape)
    state.charging.source == ChargingSource.WIRELESS -> stringResource(R.string.status_wireless_ready)
    else -> stringResource(R.string.status_charging_ready)
}
