package com.mobile.clap.dev.ml

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 音频检测配置模型
 * 定义声音检测的各项参数
 */
@Parcelize
data class AudioDetectionConfig(
    /** 触发所需的检测次数 */
    val triggerCount: Int = DEFAULT_TRIGGER_COUNT,
    
    /** 警报持续时间（秒），0表示一直响直到手动关闭 */
    val alertDurationSeconds: Int = DEFAULT_ALERT_DURATION,
    
    /** 检测灵敏度 */
    val sensitivity: Sensitivity = DEFAULT_SENSITIVITY,
    
    /** 是否启用闪光灯闪烁 */
    val flashlightEnabled: Boolean = DEFAULT_FLASHLIGHT_ENABLED,
    
    /** 是否启用震动 */
    val vibrationEnabled: Boolean = DEFAULT_VIBRATION_ENABLED,
    
    /** 是否启用口哨检测 */
    val whistleEnabled: Boolean = DEFAULT_WHISTLE_ENABLED,
    
    /** 是否启用拍手检测 */
    val handclapEnabled: Boolean = DEFAULT_HANDCLAP_ENABLED,
    
    /** 提醒音效索引 */
    val alertSoundIndex: Int = DEFAULT_ALERT_SOUND_INDEX,
    
    /** 提醒音量 (系统音量级数，0 表示关闭声音) */
    val alertVolume: Int = DEFAULT_ALERT_VOLUME
) : Parcelable {
    
    /**
     * 检测灵敏度级别
     * 
     * @param amplitude 振幅阈值（用于 DSP 检测）
     * @param dbThreshold 分贝阈值（用于 ML 检测）
     */
    enum class Sensitivity(val amplitude: Int, val dbThreshold: Int) {
        LOW(800, 50),       // 低灵敏度 - 需要较大声音（50dB）
        MEDIUM(500, 40),    // 中等灵敏度（40dB）
        HIGH(300, 30)       // 高灵敏度 - 较小声音即可触发（30dB）
    }
    
    companion object {
        const val DEFAULT_TRIGGER_COUNT = 1
        const val DEFAULT_ALERT_DURATION = 10
        val DEFAULT_SENSITIVITY = Sensitivity.MEDIUM
        const val DEFAULT_FLASHLIGHT_ENABLED = true
        const val DEFAULT_VIBRATION_ENABLED = true
        const val DEFAULT_WHISTLE_ENABLED = false
        const val DEFAULT_HANDCLAP_ENABLED = false
        const val DEFAULT_ALERT_SOUND_INDEX = 0
        const val DEFAULT_ALERT_VOLUME = 50  // 默认音量，会在首次启动时被系统音量覆盖
        
        /** 检测时间窗口（毫秒），在此时间内需要完成指定次数的检测 */
        const val DETECTION_WINDOW_MS = 3000L
        
        /** 两次检测之间的最小间隔（毫秒），防止重复触发 */
        const val MIN_DETECT_INTERVAL_MS = 100L
        
        /** 两次检测之间的最大间隔（毫秒） */
        const val MAX_DETECT_INTERVAL_MS = 1000L
        
        /**
         * 创建默认配置
         */
        fun default() = AudioDetectionConfig()
    }
}
