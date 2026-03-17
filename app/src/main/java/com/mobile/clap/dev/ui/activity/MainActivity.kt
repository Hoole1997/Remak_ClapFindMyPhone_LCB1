package com.mobile.clap.dev.ui.activity

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.mobile.clap.dev.R

class MainActivity : AppCompatActivity() {

    private var isDetectionOn = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupEdgeToEdge()
        initViews()
        setupMainToggle()
        styleSwitches()
        setupListeners()
        updateToggleUI(isDetectionOn, animate = false)
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
        }
    }

    private fun updateToggleUI(isOn: Boolean, animate: Boolean) {
        val trackBg = if (isOn) R.drawable.bg_main_toggle_track_on else R.drawable.bg_main_toggle_track_off
        toggleTrack.setBackgroundResource(trackBg)

        tvToggleLabel.text = if (isOn) "ON" else "Off"
        tvToggleLabel.setTextColor(
            if (isOn) getColor(R.color.main_purple_dark) else Color.parseColor("#C9A0E0")
        )

        tvDetectionStatus.text = if (isOn) {
            "Clap & Whistle  Detection is ON"
        } else {
            "Clap & Whistle  Detection is OFF"
        }

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
        }

        switchWhistle.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle whistle detection toggle
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
            // TODO: Open alert sound picker
        }

        findViewById<View>(R.id.itemAlertDuration).setOnClickListener {
            // TODO: Open alert duration picker
        }
    }
}
