package com.emretenoz.standby.system

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class NextAlarmState(val triggerAtMillis: Long? = null)

class NextAlarmObserver(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    val state: Flow<NextAlarmState> = callbackFlow {
        fun emitCurrent() {
            trySend(NextAlarmState(alarmManager?.nextAlarmClock?.triggerTime))
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = emitCurrent()
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED),
            ContextCompat.RECEIVER_EXPORTED,
        )
        emitCurrent()
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
