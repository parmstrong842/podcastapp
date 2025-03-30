package com.example.podcastapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


private const val tag = "BatteryLevelReceiver"

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_LOW,
            "com.example.podcastapp.ACTION_TEST_BATTERY_LOW" -> {
                Log.d(tag, "Battery low broadcast received (real or test)!")
            }
            else -> return
        }
    }
}