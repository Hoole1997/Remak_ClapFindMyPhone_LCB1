package com.mobile.clap.dev.ml

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*

/**
 * ML 音频检测器 - 检测拍手和口哨声
 * 
 * 使用 MediaPipe Tasks Audio + YAMNet 模型进行 ML 检测
 */
class MlSoundDetector(
    private val context: Context,
    private var config: AudioDetectionConfig,
    private val onDetected: () -> Unit
) {
    companion object {
        private const val TAG = "MlSoundDetector"
        
        // YAMNet 模型需要 16kHz 采样率
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = 2048
        
        // YAMNet 期望的输入长度：0.975秒 × 16kHz = 15600 样本
        private const val YAMNET_INPUT_LENGTH = 15600
    }

    private var audioRecord: AudioRecord? = null
    private var isDetecting = false
    @Volatile
    private var isPaused = false
    private var detectionJob: Job? = null
    
    // ML 分类器
    private var mlClassifier: MlAudioClassifier? = null
    
    // 音频样本累积缓冲区
    private val accumulatedSamples = ShortArray(YAMNET_INPUT_LENGTH)
    private var accumulatedCount = 0
    
    private val detectionTimestamps = mutableListOf<Long>()
    private var lastSoundTime = 0L

    /**
     * 开始检测
     */
    fun startDetection() {
        if (isDetecting) return
        
        // 初始化 ML 分类器
        mlClassifier = MlAudioClassifier.getInstance(context)
        if (!mlClassifier!!.initialize()) {
            Log.e(TAG, "Failed to initialize ML classifier")
            return
        }
        
        val bufferSize = maxOf(
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT),
            BUFFER_SIZE * 2
        )
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            Log.e(TAG, "Invalid buffer size: $bufferSize")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return
            }

            isDetecting = true
            audioRecord?.startRecording()
            
            detectionJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ShortArray(BUFFER_SIZE)
                
                while (isDetecting && isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        processAudioData(buffer, readSize)
                    }
                }
            }
            
            Log.d(TAG, "ML Detection started with sensitivity: ${config.sensitivity} (dB threshold: ${config.sensitivity.dbThreshold})")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for audio recording", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting detection", e)
        }
    }

    /**
     * 停止检测
     */
    fun stopDetection() {
        isDetecting = false
        detectionJob?.cancel()
        detectionJob = null
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio record", e)
        }
        audioRecord = null
        detectionTimestamps.clear()
        accumulatedCount = 0
        
        // 释放 ML 分类器
        mlClassifier?.release()
        mlClassifier = null
        
        Log.d(TAG, "Detection stopped")
    }

    /**
     * 处理音频数据 - 累积样本后使用 ML 分类
     */
    private var lastLogTime = 0L
    
    private fun processAudioData(buffer: ShortArray, size: Int) {
        if (isPaused) return
        val classifier = mlClassifier ?: return
        
        // 累积音频样本
        val copySize = minOf(size, YAMNET_INPUT_LENGTH - accumulatedCount)
        System.arraycopy(buffer, 0, accumulatedSamples, accumulatedCount, copySize)
        accumulatedCount += copySize
        
        // 如果累积的样本不够，继续等待
        if (accumulatedCount < YAMNET_INPUT_LENGTH) {
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        // 使用 ML 分类器进行检测
        val result = classifier.classify(
            samples = accumulatedSamples,
            sampleCount = YAMNET_INPUT_LENGTH,
            dbThreshold = config.sensitivity.dbThreshold
        )
        
        // 分类后清空缓冲区重新累积
        accumulatedCount = 0
        
        // 每秒打印一次当前状态（调试用）
        if (currentTime - lastLogTime > 1000) {
            lastLogTime = currentTime
            Log.d(TAG, "dB=${"%.1f".format(result.decibelLevel)}, threshold=${config.sensitivity.dbThreshold}")
        }
        
        // 防止连续触发
        if (currentTime - lastSoundTime < AudioDetectionConfig.MIN_DETECT_INTERVAL_MS) {
            return
        }
        
        var detected = false
        
        // 拍手检测
        if (config.handclapEnabled && result.isHandclap) {
            detected = true
            handleSoundDetection("HANDCLAP", currentTime, result.handclapConfidence)
        }
        
        // 口哨检测
        if (!detected && config.whistleEnabled && result.isWhistle) {
            detected = true
            handleSoundDetection("WHISTLE", currentTime, result.whistleConfidence)
        }
        
        if (detected) {
            lastSoundTime = currentTime
        }
    }

    /**
     * 处理声音检测（拍手或口哨）
     */
    private fun handleSoundDetection(type: String, currentTime: Long, confidence: Float) {
        // 清除过期的记录
        detectionTimestamps.removeAll { currentTime - it > AudioDetectionConfig.DETECTION_WINDOW_MS }
        
        // 检查与上一次检测的间隔是否合理
        val lastDetection = detectionTimestamps.lastOrNull()
        if (lastDetection != null) {
            val interval = currentTime - lastDetection
            if (interval > AudioDetectionConfig.MAX_DETECT_INTERVAL_MS) {
                // 间隔太长，重新开始计数
                detectionTimestamps.clear()
            }
        }
        
        // 记录这次检测
        detectionTimestamps.add(currentTime)
        
        Log.d(TAG, "$type detected (conf=${"%.2f".format(confidence)})! Count: ${detectionTimestamps.size}/${config.triggerCount}")
        
        // 检查是否达到触发条件
        if (detectionTimestamps.size >= config.triggerCount) {
            Log.d(TAG, "Trigger condition met! Triggering alert...")
            detectionTimestamps.clear()
            
            // 在主线程触发回调
            CoroutineScope(Dispatchers.Main).launch {
                onDetected()
            }
        }
    }

    /**
     * 是否正在检测
     */
    fun isDetecting(): Boolean = isDetecting

    /**
     * 暂停检测（不停止录音，仅跳过分类处理）
     */
    fun pauseDetection() {
        isPaused = true
        accumulatedCount = 0
        detectionTimestamps.clear()
        Log.d(TAG, "Detection paused")
    }

    /**
     * 恢复检测
     */
    fun resumeDetection() {
        isPaused = false
        Log.d(TAG, "Detection resumed")
    }

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: AudioDetectionConfig) {
        config = newConfig
        Log.d(TAG, "Config updated")
    }
}
