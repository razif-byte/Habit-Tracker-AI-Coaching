package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AppActivityTracker

class OverlayBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: FrameLayout? = null
    private var isExpanded = false

    companion object {
        const val CHANNEL_ID = "habit_coach_overlay_channel"
        const val NOTIFICATION_ID = 2001
        var isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundServiceNotification()
        setupFloatingWidget()
    }

    private fun startForegroundServiceNotification() {
        val channelName = "Habit Coach Overlay & Background System"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Habit Coach Overlay terapung aktif untuk pengesanan aktiviti apps dan akses pantas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Habit Coach System Monitor")
            .setContentText("Overlay terapung & pengesan aktiviti sedang aktif")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingWidget() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 200
        }

        floatingView = FrameLayout(this)

        // Root container for collapsed bubble
        val bubbleContainer = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#6750A4"))
                setStroke(4, Color.parseColor("#EADDFF"))
            }
            background = bg
            setPadding(28, 28, 28, 28)
            elevation = 16f
        }

        val bubbleText = TextView(this).apply {
            text = "⚡"
            textSize = 22f
            gravity = Gravity.CENTER
        }
        bubbleContainer.addView(bubbleText)

        // Expanded Panel
        val expandedCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val cardBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 36f
                setColor(Color.parseColor("#1C1B1F"))
                setStroke(2, Color.parseColor("#49454F"))
            }
            background = cardBg
            setPadding(36, 32, 36, 32)
            visibility = View.GONE
            elevation = 24f
        }

        val titleView = TextView(this).apply {
            text = "🎯 Habit Coach Hub"
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 8)
        }

        val statusView = TextView(this).apply {
            val summary = AppActivityTracker.getTodayAppUsage(this@OverlayBubbleService)
            text = "Masa Skrin Hari Ini: ${summary.totalScreenTimeMinutes / 60}j ${summary.totalScreenTimeMinutes % 60}m\nMedia Sosial: ${summary.socialTimeMinutes}m | AI: ${summary.aiTimeMinutes}m"
            setTextColor(Color.parseColor("#CAC4D0"))
            textSize = 12f
            setPadding(0, 0, 0, 16)
        }

        val btnTrending = Button(this).apply {
            text = "🌟 Trending Apps & Karaoke"
            setBackgroundColor(Color.parseColor("#7D5260"))
            setTextColor(Color.WHITE)
            textSize = 12f
            setOnClickListener {
                val intent = Intent(this@OverlayBubbleService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("OPEN_TAB", "trending")
                }
                startActivity(intent)
                collapsePanel(bubbleContainer, expandedCard)
            }
        }

        val btnOpenApp = Button(this).apply {
            text = "📱 Buka Aplikasi Penuh"
            setBackgroundColor(Color.parseColor("#6750A4"))
            setTextColor(Color.WHITE)
            textSize = 12f
            setOnClickListener {
                val intent = Intent(this@OverlayBubbleService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                collapsePanel(bubbleContainer, expandedCard)
            }
        }

        val btnCloseOverlay = Button(this).apply {
            text = "✖ Tutup Overlay"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#E0E0E0"))
            textSize = 11f
            setOnClickListener {
                stopSelf()
            }
        }

        expandedCard.addView(titleView)
        expandedCard.addView(statusView)
        expandedCard.addView(btnTrending)
        expandedCard.addView(btnOpenApp)
        expandedCard.addView(btnCloseOverlay)

        floatingView?.addView(bubbleContainer)
        floatingView?.addView(expandedCard)

        // Dragging & Click touch handler
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        bubbleContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        toggleExpanded(bubbleContainer, expandedCard)
                    }
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(floatingView, params)
    }

    private fun toggleExpanded(bubble: View, card: View) {
        isExpanded = !isExpanded
        if (isExpanded) {
            bubble.visibility = View.GONE
            card.visibility = View.VISIBLE
        } else {
            collapsePanel(bubble, card)
        }
    }

    private fun collapsePanel(bubble: View, card: View) {
        isExpanded = false
        card.visibility = View.GONE
        bubble.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) {
            }
            floatingView = null
        }
    }
}
