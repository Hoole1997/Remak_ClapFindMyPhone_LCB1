package com.mobile.clap.dev.ui.activity

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.mobile.clap.dev.BuildConfig
import com.mobile.clap.dev.R
import com.remax.base.ext.KvBoolDelegate

class MainActivity : AppCompatActivity() {

    private var isDetectionOn = false
    private var isUpdatingFromCode = false

    private lateinit var tvDetectionStatus: TextView
    private lateinit var toggleMainContainer: FrameLayout
    private lateinit var toggleTrack: View
    private lateinit var toggleThumb: FrameLayout
    private lateinit var tvToggleLabel: TextView

    private lateinit var switchClap: MaterialSwitch
    private lateinit var switchWhistle: MaterialSwitch
    private lateinit var switchSound: MaterialSwitch
    private lateinit var switchVibration: MaterialSwitch
    private lateinit var switchFlashlight: MaterialSwitch

    private var guideShown by KvBoolDelegate("home_guide_shown", false)

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
    }

    private fun setupMainToggle() {
        toggleMainContainer.setOnClickListener {
            isDetectionOn = !isDetectionOn
            updateToggleUI(isDetectionOn, animate = true)
            if (isDetectionOn) {
                isUpdatingFromCode = true
                switchClap.isChecked = true
                switchWhistle.isChecked = true
                isUpdatingFromCode = false
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
            // TODO: Handle clap detection toggle
            if (!isUpdatingFromCode) checkAutoOffMainToggle()
        }

        switchWhistle.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle whistle detection toggle
            if (!isUpdatingFromCode) checkAutoOffMainToggle()
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle sound toggle
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle vibration toggle
        }

        switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle flashlight toggle
        }

        findViewById<View>(R.id.itemAlertSound).setOnClickListener {
            startActivity(android.content.Intent(this, AlertSoundActivity::class.java))
        }

        findViewById<View>(R.id.itemAlertDuration).setOnClickListener {
            // TODO: Open alert duration picker
        }

    }

    private fun checkAutoOffMainToggle() {
        if (!switchClap.isChecked && !switchWhistle.isChecked && isDetectionOn) {
            isDetectionOn = false
            updateToggleUI(isDetectionOn, animate = true)
        }
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
