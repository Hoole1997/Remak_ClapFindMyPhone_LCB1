package com.mobile.clap.dev.ui.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.mobile.clap.dev.BuildConfig
import com.mobile.clap.dev.R
import com.mobile.clap.dev.ml.AlertSoundResources
import com.mobile.clap.dev.ml.AudioDetectionConfig
import com.mobile.clap.dev.service.AudioDetectionService
import com.mobile.clap.dev.ui.dialog.AlertDurationSettingDialog
import com.mobile.clap.dev.ui.dialog.MicPermissionDialog
import com.mobile.clap.dev.ui.dialog.TestMicDialog
import com.remax.base.ext.KvBoolDelegate
import com.remax.base.ext.KvIntDelegate

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var isDetectionOn = false
    private var isUpdatingFromCode = false

    private lateinit var tvDetectionStatus: TextView
    private lateinit var toggleMainContainer: FrameLayout
    private lateinit var toggleTrack: View
    private lateinit var toggleThumb: FrameLayout
    private lateinit var tvToggleLabel: TextView

    private lateinit var tvAlertDurationValue: TextView
    private lateinit var tvAlertSoundValue: TextView

    private lateinit var switchClap: MaterialSwitch
    private lateinit var switchWhistle: MaterialSwitch
    private lateinit var switchSound: MaterialSwitch
    private lateinit var switchVibration: MaterialSwitch
    private lateinit var switchFlashlight: MaterialSwitch

    private var guideShown by KvBoolDelegate("home_guide_shown", false)
    private var alertDuration by KvIntDelegate("alert_duration", 10)
    private var alertSoundIndex by KvIntDelegate("alert_sound_index", 3)
    private var alertVolumeSaved by KvIntDelegate("alert_volume", -1)

    private var clapEnabled by KvBoolDelegate("switch_clap", true)
    private var whistleEnabled by KvBoolDelegate("switch_whistle", true)
    private var soundEnabled by KvBoolDelegate("switch_sound", true)
    private var vibrationEnabled by KvBoolDelegate("switch_vibration", true)
    private var flashlightEnabled by KvBoolDelegate("switch_flashlight", true)
    private var micTestShown by KvBoolDelegate("mic_test_shown", false)
    private var pendingMicPermissionRequest = false

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                enableDetection()
                if (pendingMicPermissionRequest && !micTestShown) {
                    micTestShown = true
                    TestMicDialog.newInstance()
                        .show(supportFragmentManager, TestMicDialog.TAG)
                }
                pendingMicPermissionRequest = false
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                isDetectionOn = false
                updateToggleUI(false, animate = true)
                // 授权失败，将 Clap/Whistle 开关回退到 OFF
                isUpdatingFromCode = true
                switchClap.isChecked = false
                switchWhistle.isChecked = false
                isUpdatingFromCode = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupEdgeToEdge()
        initViews()
        setupMainToggle()
        styleSwitches()
        setupListeners()
        setupDebugEntry()
        updateToggleUI(isDetectionOn, animate = false)
        showGuideIfFirstLaunch()
    }

    private fun showGuideIfFirstLaunch() {
        if (guideShown) return

        val rootView = findViewById<FrameLayout>(R.id.main)

        // Apply blur to the main content (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (i in 0 until rootView.childCount) {
                rootView.getChildAt(i).setRenderEffect(
                    RenderEffect.createBlurEffect(25f, 25f, Shader.TileMode.CLAMP)
                )
            }
        }

        // Inflate guide overlay
        val guideOverlay = LayoutInflater.from(this)
            .inflate(R.layout.layout_home_guide_overlay, rootView, false)
        rootView.addView(guideOverlay)

        // Dismiss on tap
        guideOverlay.setOnClickListener {
            rootView.removeView(guideOverlay)
            // Remove blur
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                for (i in 0 until rootView.childCount) {
                    rootView.getChildAt(i).setRenderEffect(null)
                }
            }
            guideShown = true
        }
    }

    private fun setupEdgeToEdge() {
        val statusBarSpacer = findViewById<View>(R.id.statusBarSpacer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarSpacer.layoutParams.height = systemBars.top
            statusBarSpacer.requestLayout()
            insets
        }
    }

    private fun initViews() {
        tvDetectionStatus = findViewById(R.id.tvDetectionStatus)
        toggleMainContainer = findViewById(R.id.toggleMainContainer)
        toggleTrack = findViewById(R.id.toggleTrack)
        toggleThumb = findViewById(R.id.toggleThumb)
        tvToggleLabel = findViewById(R.id.tvToggleLabel)

        switchClap = findViewById(R.id.switchClap)
        switchWhistle = findViewById(R.id.switchWhistle)
        switchSound = findViewById(R.id.switchSound)
        switchVibration = findViewById(R.id.switchVibration)
        switchFlashlight = findViewById(R.id.switchFlashlight)
        tvAlertDurationValue = findViewById(R.id.tvAlertDurationValue)
        tvAlertSoundValue = findViewById(R.id.tvAlertSoundValue)
        updateAlertDurationDisplay()
        updateAlertSoundDisplay()

        switchClap.isChecked = clapEnabled
        switchWhistle.isChecked = whistleEnabled
        switchSound.isChecked = soundEnabled
        switchVibration.isChecked = vibrationEnabled
        switchFlashlight.isChecked = flashlightEnabled
    }

    private fun setupMainToggle() {
        toggleMainContainer.setOnClickListener {
            if (isDetectionOn) {
                // 关闭检测
                disableDetection()
            } else {
                // 开启检测 - 先检查权限
                checkPermissionAndEnable()
            }
        }
    }

    // ==================== 权限 & 检测控制 ====================

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionAndEnable() {
        val permissionsToRequest = mutableListOf<String>()

        if (!hasAudioPermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            enableDetection()
        } else {
            pendingMicPermissionRequest = !hasAudioPermission()
            MicPermissionDialog.newInstance()
                .setOnGotItListener {
                    requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                }
                .setOnCancelListener {
                    // 用户取消，不做任何事
                }
                .show(supportFragmentManager, MicPermissionDialog.TAG)
        }
    }

    private fun enableDetection() {
        isDetectionOn = true
        updateToggleUI(true, animate = true)

        // 总开关开启时，同时开启 Clap 和 Whistle（仅当两个都关闭时）
        if (!switchClap.isChecked && !switchWhistle.isChecked) {
            isUpdatingFromCode = true
            switchClap.isChecked = true
            switchWhistle.isChecked = true
            clapEnabled = true
            whistleEnabled = true
            isUpdatingFromCode = false
        }

        // 启动前台检测服务（config 直接随 START intent 传递，避免竞态）
        val config = buildDetectionConfig()
        Log.d(TAG, "Detection enabled, config: clap=${config.handclapEnabled}, whistle=${config.whistleEnabled}, alertVol=${config.alertVolume}, soundIdx=${config.alertSoundIndex}")
        AudioDetectionService.start(this, config)
    }

    private fun disableDetection() {
        isDetectionOn = false
        updateToggleUI(false, animate = true)

        // 总开关关闭时，同时关闭 Clap 和 Whistle
        isUpdatingFromCode = true
        switchClap.isChecked = false
        switchWhistle.isChecked = false
        clapEnabled = false
        whistleEnabled = false
        isUpdatingFromCode = false

        // 停止前台检测服务
        AudioDetectionService.stop(this)
        Log.d(TAG, "Detection disabled, service stopped")
    }

    private fun buildDetectionConfig(): AudioDetectionConfig {
        return AudioDetectionConfig(
            handclapEnabled = clapEnabled && isDetectionOn,
            whistleEnabled = whistleEnabled && isDetectionOn,
            alertDurationSeconds = alertDuration,
            alertSoundIndex = alertSoundIndex,
            vibrationEnabled = vibrationEnabled,
            flashlightEnabled = flashlightEnabled,
            alertVolume = if (soundEnabled) {
                val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
                val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                if (alertVolumeSaved < 0) maxVol else alertVolumeSaved.coerceIn(1, maxVol)
            } else 0
        )
    }

    private fun updateServiceConfig() {
        if (!isDetectionOn) return
        val config = buildDetectionConfig()
        AudioDetectionService.updateConfig(this, config)
        Log.d(TAG, "Config updated: clap=${config.handclapEnabled}, whistle=${config.whistleEnabled}")
    }

    /**
     * 根据 Clap/Whistle 开关状态同步总开关
     */
    private fun syncMainToggleWithDetectionSwitches(switchTurnedOn: Boolean) {
        if (switchTurnedOn) {
            // 任一开关打开时，如果总开关是关的，则开启总开关
            if (!isDetectionOn) {
                checkPermissionAndEnable()
            } else {
                updateServiceConfig()
            }
        } else {
            // 两个都关闭时，关闭总开关
            if (!switchClap.isChecked && !switchWhistle.isChecked) {
                if (isDetectionOn) {
                    disableDetection()
                }
            } else {
                updateServiceConfig()
            }
        }
    }

    private fun updateToggleUI(isOn: Boolean, animate: Boolean) {
        val trackBg = if (isOn) R.drawable.bg_main_toggle_track_on else R.drawable.bg_main_toggle_track_off
        toggleTrack.setBackgroundResource(trackBg)

        tvToggleLabel.text = getString(if (isOn) R.string.home_toggle_on else R.string.home_toggle_off)
        tvToggleLabel.setTextColor(
            if (isOn) getColor(R.color.main_purple_dark) else Color.parseColor("#C9A0E0")
        )

        tvDetectionStatus.text = getString(
            if (isOn) R.string.home_detection_on else R.string.home_detection_off
        )

        val thumbParams = toggleThumb.layoutParams as FrameLayout.LayoutParams
        val newGravity = if (isOn) {
            Gravity.CENTER_VERTICAL or Gravity.END
        } else {
            Gravity.CENTER_VERTICAL or Gravity.START
        }

        if (animate) {
            val containerWidth = toggleMainContainer.width
            val thumbWidth = toggleThumb.width
            val margin = (4 * resources.displayMetrics.density).toInt()
            val startX = toggleThumb.translationX
            val endX = if (isOn) {
                0f
            } else {
                -(containerWidth - thumbWidth - margin * 2).toFloat()
            }

            // Reset gravity first, then animate position
            thumbParams.gravity = if (isOn) {
                Gravity.CENTER_VERTICAL or Gravity.END
            } else {
                Gravity.CENTER_VERTICAL or Gravity.END
            }
            thumbParams.marginEnd = margin
            thumbParams.marginStart = margin
            toggleThumb.layoutParams = thumbParams

            val animator = ObjectAnimator.ofFloat(toggleThumb, "translationX", startX, endX)
            animator.duration = 200
            animator.start()
        } else {
            thumbParams.gravity = newGravity
            thumbParams.marginEnd = (4 * resources.displayMetrics.density).toInt()
            thumbParams.marginStart = (4 * resources.displayMetrics.density).toInt()
            toggleThumb.layoutParams = thumbParams
            toggleThumb.translationX = 0f
        }
    }

    private fun styleSwitches() {
        // Clap switch - orange track
        styleSwitchColors(
            switchClap,
            checkedTrackColor = getColor(R.color.switch_orange_track),
            uncheckedTrackColor = getColor(R.color.switch_off_orange_track)
        )

        // Whistle switch - blue track
        styleSwitchColors(
            switchWhistle,
            checkedTrackColor = getColor(R.color.switch_purple_track),
            uncheckedTrackColor = getColor(R.color.switch_off_purple_track)
        )

        // Sound switch - blue track (currently off)
        styleSwitchColors(
            switchSound,
            checkedTrackColor = getColor(R.color.switch_blue_track),
            uncheckedTrackColor = getColor(R.color.switch_off_track)
        )

        // Vibration switch - blue track
        styleSwitchColors(
            switchVibration,
            checkedTrackColor = getColor(R.color.switch_blue_track),
            uncheckedTrackColor = getColor(R.color.switch_off_track)
        )

        // Flashlight switch - blue track
        styleSwitchColors(
            switchFlashlight,
            checkedTrackColor = getColor(R.color.switch_blue_track),
            uncheckedTrackColor = getColor(R.color.switch_off_track)
        )
    }

    private fun styleSwitchColors(
        switch: MaterialSwitch,
        checkedTrackColor: Int,
        uncheckedTrackColor: Int
    ) {
        val trackColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(checkedTrackColor, uncheckedTrackColor)
        )
        val thumbColorStateList = ColorStateList.valueOf(Color.WHITE)

        switch.trackTintList = trackColorStateList
        switch.thumbTintList = thumbColorStateList
        switch.trackDecorationTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    }

    private fun setupListeners() {
        switchClap.setOnCheckedChangeListener { _, isChecked ->
            clapEnabled = isChecked
            if (!isUpdatingFromCode) syncMainToggleWithDetectionSwitches(isChecked)
        }

        switchWhistle.setOnCheckedChangeListener { _, isChecked ->
            whistleEnabled = isChecked
            if (!isUpdatingFromCode) syncMainToggleWithDetectionSwitches(isChecked)
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            soundEnabled = isChecked
            updateServiceConfig()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            vibrationEnabled = isChecked
            updateServiceConfig()
        }

        switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
            flashlightEnabled = isChecked
            updateServiceConfig()
        }

        findViewById<View>(R.id.itemAlertSound).setOnClickListener {
            startActivity(android.content.Intent(this, AlertSoundActivity::class.java))
        }

        findViewById<View>(R.id.itemAlertDuration).setOnClickListener {
            AlertDurationSettingDialog.newInstance()
                .setSelectedDuration(alertDuration)
                .setOnConfirmListener { duration ->
                    alertDuration = duration
                    updateAlertDurationDisplay()
                }
                .show(supportFragmentManager, AlertDurationSettingDialog.TAG)
        }

    }

    private fun updateAlertDurationDisplay() {
        tvAlertDurationValue.text = getString(R.string.alert_duration_seconds, alertDuration)
    }

    private fun updateAlertSoundDisplay() {
        tvAlertSoundValue.text = AlertSoundResources.getDisplayName(alertSoundIndex)
    }

    override fun onResume() {
        super.onResume()
        updateAlertSoundDisplay()
        // 从 AlertSoundActivity 返回后，将最新配置推送给服务
        updateServiceConfig()
    }

    private fun setupDebugEntry() {
        if (BuildConfig.DEBUG) {
            findViewById<TextView>(R.id.tvAppName).setOnLongClickListener {
                startActivity(android.content.Intent(this, DebugActivity::class.java))
                true
            }
        }
    }
}
