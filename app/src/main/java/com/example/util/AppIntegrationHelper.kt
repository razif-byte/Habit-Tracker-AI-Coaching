package com.example.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import com.example.service.OverlayBubbleService
import java.net.URLEncoder

object AppIntegrationHelper {

    fun launchExternalAppOrWeb(context: Context, packageName: String, url: String) {
        try {
            if (packageName.isNotBlank()) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                }
            }

            // Fallback to browser or market
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Try opening in Play Store if package was specified
            if (packageName.isNotBlank()) {
                openPlayStore(context, packageName)
            } else {
                Toast.makeText(context, "Tidak dapat membuka pautan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchKaraokeYouTube(context: Context, query: String, fallbackUrl: String) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val appUri = Uri.parse("vnd.youtube.results:$encoded")
            val ytIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (ytIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(ytIntent)
                return
            }
        } catch (_: Exception) {
        }

        // Web fallback
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Sila pasang aplikasi YouTube atau pelayar web", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchKaraokeSmule(context: Context, songTitle: String, fallbackUrl: String) {
        try {
            val smuleIntent = context.packageManager.getLaunchIntentForPackage("com.smule.singandroid")
            if (smuleIntent != null) {
                smuleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(smuleIntent)
                return
            }
        } catch (_: Exception) {
        }

        // Try Smule web / search URL
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            openPlayStore(context, "com.smule.singandroid")
        }
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webStoreIntent)
        }
    }

    fun shareContent(context: Context, title: String, message: String, url: String) {
        try {
            val shareText = if (url.isNotBlank()) "$title\n$message\n$url" else "$title\n$message"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Kongsi melalui").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ralat berkongsi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun isUsageAccessPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun toggleOverlayService(context: Context, enable: Boolean) {
        val intent = Intent(context, OverlayBubbleService::class.java)
        if (enable) {
            if (isOverlayPermissionGranted(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                openOverlaySettings(context)
                Toast.makeText(context, "Sila benarkan 'Paparan di atas aplikasi lain' (Overlay)", Toast.LENGTH_LONG).show()
            }
        } else {
            context.stopService(intent)
        }
    }
}
