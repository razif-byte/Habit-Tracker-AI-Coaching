package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

class HabitNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "habit_reminders_channel"
        const val CHANNEL_MILESTONES = "habit_milestones_channel"
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_MILESTONE = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit schedule reminders and coach nudges"
            }

            val milestoneChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Streak & Milestones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Celebrations for streak achievements and badge unlocks"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.let {
                it.createNotificationChannel(reminderChannel)
                it.createNotificationChannel(milestoneChannel)
            }
        }
    }

    fun sendHabitReminder(habitTitle: String, reminderTime: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Habit Time: $habitTitle")
            .setContentText("Keep your momentum alive! Scheduled for $reminderTime today.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Keep your momentum alive! You are only one check-in away from locking in today's streak for '$habitTitle'.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (_: SecurityException) {}
    }

    fun sendMilestoneCelebration(title: String, streakDays: Int, xpEarned: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONES)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle("🔥 Milestone Unlocked: $streakDays-Day Streak!")
            .setContentText("Incredible discipline! You earned +$xpEarned XP for $title.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🔥 You have hit a $streakDays-day streak for '$title'!\nYou earned +$xpEarned XP. Coach Zenith salutes your relentless consistency.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MILESTONE, notification)
        } catch (_: SecurityException) {}
    }
}
