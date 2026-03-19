package com.mobile.clap.dev.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mobile.clap.dev.R
import com.mobile.clap.dev.manager.DeviceAlertManager
import com.mobile.clap.dev.ml.AudioDetectionConfig
import com.mobile.clap.dev.ml.MlSoundDetector
import com.mobile.clap.dev.ui.activity.MainActivity

/**
 * 音频检测前台服务
 * 在后台持续监听拍手/口哨声并触发警报
 */
class AudioDetectionService : Service() {

    companion object {
        private const val TAG = "AudioDetectionService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "audio_detection_channel"

        const val ACTION_START = "com.mobile.clap.dev.START_DETECTION"
        const val ACTION_STOP = "com.mobile.clap.dev.STOP_DETECTION"
        const val ACTION_STOP_ALERT = "com.mobile.clap.dev.STOP_ALERT"

        private const val ACTION_UPDATE_CONFIG = "com.mobile.clap.dev.UPDATE_CONFIG"
        private const val ACTION_PAUSE_DETECTION = "com.mobile.clap.dev.PAUSE_DETECTION"
        private const val ACTION_RESUME_DETECTION = "com.mobile.clap.dev.RESUME_DETECTION"
        private const val EXTRA_CONFIG = "extra_config"

        fun start(context: Context, config: AudioDetectionConfig? = null) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_START
                config?.let { putExtra(EXTRA_CONFIG, it) }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun stopAlert(context: Context) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_STOP_ALERT
            }
            context.startService(intent)
        }

        fun updateConfig(context: Context, config: AudioDetectionConfig) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_UPDATE_CONFIG
                putExtra(EXTRA_CONFIG, config)
            }
            context.startService(intent)
        }

        fun pauseDetection(context: Context) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_PAUSE_DETECTION
            }
            context.startService(intent)
        }

        fun resumeDetection(context: Context) {
            val intent = Intent(context, AudioDetectionService::class.java).apply {
                action = ACTION_RESUME_DETECTION
            }
            context.startService(intent)
        }
    }

    private var audioDetector: MlSoundDetector? = null
    private var alertManager: DeviceAlertManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var config = AudioDetectionConfig.default()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                // 从 START intent 中提取 config（确保启动时使用正确的配置）
                @Suppress("DEPRECATION")
                val startConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CONFIG, AudioDetectionConfig::class.java)
                } else {
                    intent.getParcelableExtra(EXTRA_CONFIG)
                }
                startConfig?.let { config = it }
                startDetection()
            }
            ACTION_STOP -> stopDetection()
            ACTION_STOP_ALERT -> stopCurrentAlert()
            ACTION_UPDATE_CONFIG -> {
                @Suppress("DEPRECATION")
                val newConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CONFIG, AudioDetectionConfig::class.java)
                } else {
                    intent.getParcelableExtra(EXTRA_CONFIG)
                }
                newConfig?.let { handleConfigUpdate(it) }
            }
            ACTION_PAUSE_DETECTION -> audioDetector?.pauseDetection()
            ACTION_RESUME_DETECTION -> audioDetector?.resumeDetection()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection()
        Log.d(TAG, "Service destroyed")
    }

    private fun startDetection() {
        Log.d(TAG, "Starting detection... config=$config")
        Log.d(TAG, "clap=${config.handclapEnabled}, whistle=${config.whistleEnabled}, alertVol=${config.alertVolume}, soundIdx=${config.alertSoundIndex}")

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification())

        // 获取 WakeLock 保持 CPU 运行
        acquireWakeLock()

        // 初始化警报管理器
        alertManager = DeviceAlertManager(this, config) {
            // 警报自动停止时刷新通知
            onAlertAutoStopped()
        }

        // 初始化音频检测器
        audioDetector = MlSoundDetector(this, config) {
            onSoundDetected()
        }
        audioDetector?.startDetection()
    }

    private fun stopDetection() {
        Log.d(TAG, "Stopping detection...")

        audioDetector?.stopDetection()
        audioDetector = null

        alertManager?.release()
        alertManager = null

        releaseWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopCurrentAlert() {
        Log.d(TAG, "Stopping current alert...")
        alertManager?.stopAlert()

        // 更新通知状态
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun onAlertAutoStopped() {
        Log.d(TAG, "Alert auto stopped, refreshing notification...")
        // 更新通知状态
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun handleConfigUpdate(newConfig: AudioDetectionConfig) {
        Log.d(TAG, "Config updated: $newConfig")
        config = newConfig
        alertManager?.updateConfig(newConfig)
        audioDetector?.updateConfig(newConfig)
    }

    private fun onSoundDetected() {
        Log.d(TAG, "Sound detected! Triggering alert...")

        // 更新通知显示警报状态
        val notification = createAlertingNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // 触发警报
        alertManager?.startAlert()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "clapfindmyphone:AudioDetectionWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 最多10分钟
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createAlertingNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AudioDetectionService::class.java).apply {
                action = ACTION_STOP_ALERT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallView = RemoteViews(packageName, R.layout.notification_alerting_small)
        smallView.setOnClickPendingIntent(R.id.btnDisableDetection, stopIntent)

        val bigView = RemoteViews(packageName, R.layout.notification_alerting)
        bigView.setOnClickPendingIntent(R.id.btnDisableDetection, stopIntent)

        val headsUpView = RemoteViews(packageName, R.layout.notification_alerting_headsup)
        headsUpView.setOnClickPendingIntent(R.id.btnDisableDetection, stopIntent)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(smallView)
            .setCustomBigContentView(bigView)
            .setCustomHeadsUpContentView(headsUpView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}
