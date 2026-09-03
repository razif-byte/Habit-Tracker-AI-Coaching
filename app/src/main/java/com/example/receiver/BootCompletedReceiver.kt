package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.notification.HabitNotificationManager
import com.example.service.OverlayBubbleService
import com.example.util.AppIntegrationHelper

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
        const val PREFS_NAME = "habit_coach_system_prefs"
        const val KEY_AUTOSTART_OVERLAY = "key_autostart_overlay"
        const val KEY_BOOT_MONITORING = "key_boot_monitoring"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Device booted / package replaced with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val shouldStartOverlay = prefs.getBoolean(KEY_AUTOSTART_OVERLAY, true)

            // 1. Reschedule notifications and habit checks
            try {
                val notificationManager = HabitNotificationManager(context)
                notificationManager.sendHabitReminder("Rutin Pagi & Tabiat", "08:00")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders on boot", e)
            }

            // 2. Start Floating Overlay if enabled and permission granted
            if (shouldStartOverlay && AppIntegrationHelper.isOverlayPermissionGranted(context)) {
                val serviceIntent = Intent(context, OverlayBubbleService::class.java)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d(TAG, "Overlay service launched upon system boot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Could not start overlay on boot", e)
                }
            }
        }
    }
}
