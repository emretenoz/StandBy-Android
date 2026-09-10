package com.example.standby.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ChargingSource { NONE, WIRED, WIRELESS, OTHER }

data class ChargingState(
    val isCharging: Boolean = false,
    val batteryPercent: Int = 0,
    val source: ChargingSource = ChargingSource.NONE,
)

class ChargingObserver(private val context: Context) {
    private val batteryManager = context.getSystemService(BatteryManager::class.java)

    val state: Flow<ChargingState> = callbackFlow {
        fun sendBatteryState(intent: Intent?) {
            if (intent == null) return
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            val reportsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                batteryManager?.isCharging == true
            val source = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
                BatteryManager.BATTERY_PLUGGED_AC,
                BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.WIRED
                else -> if (reportsCharging) ChargingSource.OTHER else ChargingSource.NONE
            }
            trySend(
                ChargingState(
                    isCharging = reportsCharging,
                    batteryPercent = (level * 100 / scale).coerceIn(0, 100),
                    source = source,
                )
            )
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val batteryIntent = if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    intent
                } else {
                    this@ChargingObserver.context.registerReceiver(
                        null,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                    )
                }
                sendBatteryState(batteryIntent)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        val initial = context.registerReceiver(receiver, filter)
        sendBatteryState(initial)
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
