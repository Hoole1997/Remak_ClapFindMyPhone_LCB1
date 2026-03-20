package com.mobile.clap.dev.ml

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.core.RunningMode
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.components.containers.AudioData.AudioDataFormat
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.core.BaseOptions
import kotlin.math.log10

/**
 * ML 音频分类器 - 使用 MediaPipe Tasks Audio 进行音频分类
 * 
 * 基于 YAMNet 模型识别拍手、口哨等声音
 * 使用 MediaPipe 替代 TensorFlow Lite Task Audio 以支持 16KB 页面对齐
 */
class MlAudioClassifier(private val context: Context) {
    
    companion object {
        private const val TAG = "MlAudioClassifier"
        private const val MODEL_FILE = "yamnet.tflite"
        
        // 置信度阈值
        private const val BASE_SCORE_THRESHOLD = 0.15f
        private const val CONFIRM_CONFIDENCE_THRESHOLD = 0.3f
        
        // 最大返回结果数
        private const val MAX_RESULTS = 5
        
        // 采样率（YAMNet 期望 16kHz）
        const val SAMPLE_RATE = 16000
        
        // 目标声音类别（YAMNet 标签）
        private val HANDCLAP_LABELS = setOf(
            "Hands",
            "Clapping", 
            "Finger snapping",
            "Slap, smack"
        )
        
        private val WHISTLE_LABELS = setOf(
            "Whistle",
            "Whistling"
        )
        
        @Volatile
        private var instance: MlAudioClassifier? = null
        
        fun getInstance(context: Context): MlAudioClassifier {
            return instance ?: synchronized(this) {
                instance ?: MlAudioClassifier(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private var audioClassifier: AudioClassifier? = null
    private var isInitialized = false
    
    /**
     * 分类结果
     */
    data class ClassifyResult(
        val isHandclap: Boolean = false,
        val isWhistle: Boolean = false,
        val handclapConfidence: Float = 0f,
        val whistleConfidence: Float = 0f,
        val decibelLevel: Float = 0f,
        val topCategories: List<Category> = emptyList()
    )
    
    /**
     * 初始化分类器
     */
    fun initialize(): Boolean {
        if (isInitialized) return true
        
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_FILE)
                .build()
            
            val options = AudioClassifier.AudioClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.AUDIO_CLIPS)
                .setMaxResults(MAX_RESULTS)
                .setScoreThreshold(BASE_SCORE_THRESHOLD)
                .build()
            
            audioClassifier = AudioClassifier.createFromOptions(context, options)
            isInitialized = true
            
            Log.d(TAG, "Initialized successfully with MediaPipe")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize: ${e.message}", e)
            isInitialized = false
            false
        }
    }
    
    /**
     * 是否已初始化
     */
    fun isReady(): Boolean = isInitialized && audioClassifier != null
    
    /**
     * 对音频样本进行分类
     * 
     * @param samples 音频样本（16-bit PCM）
     * @param sampleCount 样本数量
     * @param dbThreshold 分贝阈值
     * @return 分类结果
     */
    fun classify(samples: ShortArray, sampleCount: Int, dbThreshold: Int): ClassifyResult {
        if (!isReady()) {
            Log.w(TAG, "Classifier not ready")
            return ClassifyResult()
        }
        
        val classifier = audioClassifier ?: return ClassifyResult()
        
        // 计算分贝值
        val decibelLevel = calculateDecibels(samples, sampleCount)
        
        // 如果分贝低于阈值，直接返回
        if (decibelLevel < dbThreshold) {
            return ClassifyResult(decibelLevel = decibelLevel)
        }
        
        return try {
            // 转换为 float 样本并归一化到 [-1, 1]
            val floatSamples = FloatArray(sampleCount) { i ->
                samples[i] / 32768f
            }
            
            // 创建 AudioData（使用 16kHz 采样率）
            val audioFormat = AudioDataFormat.builder()
                .setNumOfChannels(1)
                .setSampleRate(SAMPLE_RATE.toFloat())
                .build()
            val audioData = AudioData.create(audioFormat, sampleCount)
            audioData.load(floatSamples, 0, sampleCount)
            
            // 运行分类
            val result = classifier.classify(audioData) ?: return ClassifyResult(decibelLevel = decibelLevel)
            
            if (result.classificationResults().isEmpty()) {
                return ClassifyResult(decibelLevel = decibelLevel)
            }
            
            val classifications = result.classificationResults().firstOrNull()
                ?: return ClassifyResult(decibelLevel = decibelLevel)
            val categories = classifications.classifications().firstOrNull()?.categories()
                ?: return ClassifyResult(decibelLevel = decibelLevel)
            
            // 查找拍手和口哨的置信度
            var handclapConfidence = 0f
            var whistleConfidence = 0f
            
            for (category in categories) {
                val label = category.categoryName() ?: continue
                val score = category.score()
                
                if (HANDCLAP_LABELS.any { it.equals(label, ignoreCase = true) }) {
                    handclapConfidence = maxOf(handclapConfidence, score)
                }
                if (WHISTLE_LABELS.any { it.equals(label, ignoreCase = true) }) {
                    whistleConfidence = maxOf(whistleConfidence, score)
                }
            }
            
            val isHandclap = handclapConfidence >= CONFIRM_CONFIDENCE_THRESHOLD
            val isWhistle = whistleConfidence >= CONFIRM_CONFIDENCE_THRESHOLD
            
            if (isHandclap || isWhistle) {
                Log.d(TAG, "Detection: handclap=${"%.2f".format(handclapConfidence)}, " +
                        "whistle=${"%.2f".format(whistleConfidence)}, dB=${"%.1f".format(decibelLevel)}")
            }
            
            ClassifyResult(
                isHandclap = isHandclap,
                isWhistle = isWhistle,
                handclapConfidence = handclapConfidence,
                whistleConfidence = whistleConfidence,
                decibelLevel = decibelLevel,
                topCategories = categories.take(3)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Classification error: ${e.message}", e)
            ClassifyResult(decibelLevel = decibelLevel)
        }
    }
    
    /**
     * 计算分贝值
     * dB = 10 * log10(sum(sample^2) / length)
     */
    private fun calculateDecibels(samples: ShortArray, length: Int): Float {
        if (length <= 0) return 0f
        
        var sumSquared = 0L
        for (i in 0 until length) {
            val sample = samples[i].toLong()
            sumSquared += sample * sample
        }
        
        val meanSquared = sumSquared.toFloat() / length
        return if (meanSquared > 0) {
            (10 * log10(meanSquared.toDouble())).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        try {
            audioClassifier?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing classifier", e)
        }
        audioClassifier = null
        isInitialized = false
        instance = null
        Log.d(TAG, "Released")
    }
}
