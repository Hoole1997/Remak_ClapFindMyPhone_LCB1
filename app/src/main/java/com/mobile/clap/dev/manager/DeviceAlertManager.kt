package com.mobile.clap.dev.manager

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.mobile.clap.dev.ml.AlertSoundResources
import com.mobile.clap.dev.ml.AudioDetectionConfig
import kotlinx.coroutines.*

/**
 * 设备警报管理器 - 处理铃声、震动、闪光灯
 */
class DeviceAlertManager(
    private val context: Context,
    private var config: AudioDetectionConfig,
    private val onAlertFinished: (() -> Unit)? = null
) {
    companion object {
        private const val TAG = "DeviceAlertManager"
        private const val TORCH_BLINK_INTERVAL_MS = 300L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var rearCameraId: String? = null
    
    private var isRunning = false
    private var torchJob: Job? = null
    private var autoStopJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        setupVibrator()
        setupCamera()
    }

    private fun setupVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun setupCamera() {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            rearCameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup camera", e)
        }
    }

    /**
     * 开始警报
     */
    fun startAlert() {
        if (isRunning) return
        isRunning = true
        
        Log.d(TAG, "Starting alert... alertVolume=${config.alertVolume}, soundIdx=${config.alertSoundIndex}, vibration=${config.vibrationEnabled}, flashlight=${config.flashlightEnabled}")
        
        // 播放铃声（检查声音是否启用）
        if (config.alertVolume > 0) {
            playAlarmSound()
        }
        
        // 震动
        if (config.vibrationEnabled) {
            beginVibration()
        }
        
        // 闪光灯
        if (config.flashlightEnabled) {
            beginTorchBlink()
        }
        
        // 设置自动停止
        if (config.alertDurationSeconds > 0) {
            autoStopJob = CoroutineScope(Dispatchers.Main).launch {
                delay(config.alertDurationSeconds * 1000L)
                stopAlert(notifyCallback = true)
            }
        }
    }

    /**
     * 停止警报
     * @param notifyCallback 是否通知回调（自动停止时为 true）
     */
    fun stopAlert(notifyCallback: Boolean = false) {
        if (!isRunning) return
        isRunning = false
        
        Log.d(TAG, "Stopping alert... notifyCallback=$notifyCallback")
        
        autoStopJob?.cancel()
        autoStopJob = null
        
        silenceAlarm()
        cancelVibration()
        stopTorchBlink()
        
        // 自动停止时通知回调刷新通知
        if (notifyCallback) {
            onAlertFinished?.invoke()
        }
    }

    /**
     * 播放警报铃声
     */
    private fun playAlarmSound() {
        try {
            // 根据配置选择音效资源
            val soundResId = AlertSoundResources.getAudioResId(config.alertSoundIndex)

            // 使用 STREAM_ALARM 确保后台/勿扰模式下也能播放，按用户设置的音量比例换算
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxAlarmVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val maxMusicVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val alarmVol = if (maxMusicVol > 0) {
                ((config.alertVolume.toFloat() / maxMusicVol) * maxAlarmVol).toInt()
                    .coerceIn(1, maxAlarmVol)
            } else {
                maxAlarmVol
            }
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVol, 0)

            // 必须在 prepare() 之前设置 AudioAttributes，否则会抛 IllegalStateException
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(attributes)
                context.resources.openRawResourceFd(soundResId)?.use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                isLooping = true
                prepare()
                start()
            }

            Log.d(TAG, "Alarm playing, sound index: ${config.alertSoundIndex}, volume: $maxAlarmVol")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm sound", e)
        }
    }
    
    /**
     * 停止铃声
     */
    private fun silenceAlarm() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            Log.d(TAG, "Alarm silenced")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to silence alarm", e)
        }
    }

    /**
     * 开始震动
     */
    private fun beginVibration() {
        try {
            val pattern = longArrayOf(0, 500, 200, 500) // 震动模式
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
            
            Log.d(TAG, "Vibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration", e)
        }
    }

    /**
     * 停止震动
     */
    private fun cancelVibration() {
        try {
            vibrator?.cancel()
            Log.d(TAG, "Vibration cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel vibration", e)
        }
    }

    /**
     * 开始闪光灯闪烁
     */
    private fun beginTorchBlink() {
        torchJob = CoroutineScope(Dispatchers.IO).launch {
            var torchOn = false
            while (isRunning && isActive) {
                try {
                    rearCameraId?.let { id ->
                        cameraManager?.setTorchMode(id, torchOn)
                    }
                    torchOn = !torchOn
                    delay(TORCH_BLINK_INTERVAL_MS)
                } catch (e: CameraAccessException) {
                    Log.e(TAG, "Failed to toggle torch", e)
                    break
                }
            }
        }
        Log.d(TAG, "Torch blink started")
    }

    /**
     * 停止闪光灯
     */
    private fun stopTorchBlink() {
        torchJob?.cancel()
        torchJob = null
        
        try {
            rearCameraId?.let { id ->
                cameraManager?.setTorchMode(id, false)
            }
            Log.d(TAG, "Torch blink stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop torch", e)
        }
    }

    /**
     * 是否正在警报中
     */
    fun isRunning(): Boolean = isRunning

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: AudioDetectionConfig) {
        config = newConfig
        Log.d(TAG, "Config updated")
    }

    /**
     * 释放资源
     */
    fun release() {
        stopAlert()
    }
}
