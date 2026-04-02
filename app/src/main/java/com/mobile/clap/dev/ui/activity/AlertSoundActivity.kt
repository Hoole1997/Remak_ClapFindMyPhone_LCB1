package com.mobile.clap.dev.ui.activity

import android.graphics.Rect
import android.util.Log
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.common.bill.ads.ext.AdShowExt
import androidx.lifecycle.lifecycleScope
import com.android.common.bill.ads.ext.AdShowExt.loadInterstitial
import kotlinx.coroutines.launch
import com.mobile.clap.dev.R
import com.mobile.clap.dev.service.AudioDetectionService
import com.remax.base.ext.KvIntDelegate

class AlertSoundActivity : AppCompatActivity() {

    private lateinit var seekBarVolume: SeekBar
    private lateinit var tvVolumePercent: TextView
    private lateinit var rvSounds: RecyclerView
    private lateinit var btnSave: TextView
    private lateinit var adBannerContainer: FrameLayout

    private lateinit var audioMgr: AudioManager
    private var previewPlayer: MediaPlayer? = null

    private var savedSoundIndex by KvIntDelegate("alert_sound_index", 3)
    private var savedVolume by KvIntDelegate("alert_volume", -1)

    private var selectedSoundIndex = 3
    private var currentVolume = 0

    private val soundItems = listOf(
        SoundItem("LABUBU", R.mipmap.img_setting_labubu, R.raw.alert_labubu, hasHot = true, hasAd = true),
        SoundItem("APT", R.mipmap.img_setting_apt, R.raw.alert_apt, hasAd = true),
        SoundItem("Music", R.mipmap.img_setting_music, R.raw.alert_music, hasAd = true),
        SoundItem("Cat", R.mipmap.img_setting_cat, R.raw.alert_cat),
        SoundItem("Dog", R.mipmap.img_setting_dog, R.raw.alert_dog),
        SoundItem("Alarm", R.mipmap.img_setting_alarm, R.raw.alert_waring),
        SoundItem("Hello", R.mipmap.img_setting_hello, R.raw.alert_whistle),
        SoundItem("Whistle", R.mipmap.img_setting_whistle, R.raw.alert_bell),
        SoundItem("Gunshot", R.mipmap.img_setting_gunshot, R.raw.alert_gunshot),
        SoundItem("Piano", R.mipmap.img_setting_piano, R.raw.alert_piano),
        SoundItem("Train", R.mipmap.img_setting_train, R.raw.alert_train),
        SoundItem("Warning", R.mipmap.img_setting_warning, R.raw.alert_alarm)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert_sound)

        audioMgr = getSystemService(AUDIO_SERVICE) as AudioManager

        selectedSoundIndex = savedSoundIndex
        val maxVol = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        currentVolume = if (savedVolume < 0) audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC) else savedVolume.coerceIn(0, maxVol)

        setupEdgeToEdge()
        initViews()
        setupVolumeSeekBar()
        setupSoundGrid()
        setupListeners()
        loadNativeAd()
    }

    private fun setupEdgeToEdge() {
        val statusBarSpacer = findViewById<View>(R.id.statusBarSpacer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarSpacer.layoutParams.height = systemBars.top
            statusBarSpacer.requestLayout()
            insets
        }
    }

    private fun initViews() {
        seekBarVolume = findViewById(R.id.seekBarVolume)
        tvVolumePercent = findViewById(R.id.tvVolumePercent)
        rvSounds = findViewById(R.id.rvSounds)
        btnSave = findViewById(R.id.btnSave)
        adBannerContainer = findViewById(R.id.adBannerContainer)
    }

    private fun setupVolumeSeekBar() {
        val maxVol = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        seekBarVolume.max = maxVol
        seekBarVolume.progress = currentVolume
        updateVolumePercent(currentVolume, maxVol)

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress
                updateVolumePercent(progress, maxVol)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playPreviewSound(soundItems[selectedSoundIndex].rawResId)
            }
        })
    }

    private fun updateVolumePercent(volume: Int, maxVolume: Int) {
        val percent = (volume * 100f / maxVolume).toInt()
        tvVolumePercent.text = "$percent%"
    }

    private fun setupSoundGrid() {
        val layoutManager = GridLayoutManager(this, 4)
        rvSounds.layoutManager = layoutManager
        rvSounds.isNestedScrollingEnabled = false

        val rowSpacing = (16 * resources.displayMetrics.density).toInt()
        rvSounds.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position >= 4) {
                    outRect.top = rowSpacing
                }
            }
        })

        (rvSounds.itemAnimator as? androidx.recyclerview.widget.DefaultItemAnimator)
            ?.supportsChangeAnimations = false
        rvSounds.adapter = SoundAdapter()
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            savedSoundIndex = selectedSoundIndex
            savedVolume = currentVolume
            finish()
        }
    }

    data class SoundItem(
        val name: String,
        val iconRes: Int,
        val rawResId: Int,
        val hasHot: Boolean = false,
        val hasAd: Boolean = false
    )

    private inner class SoundAdapter : RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val flSoundIconContainer: FrameLayout = itemView.findViewById(R.id.flSoundIconContainer)
            val ivSoundIcon: ImageView = itemView.findViewById(R.id.ivSoundIcon)
            val ivHotTag: ImageView = itemView.findViewById(R.id.ivHotTag)
            val tvAdTag: TextView = itemView.findViewById(R.id.tvAdTag)
            val tvSoundName: TextView = itemView.findViewById(R.id.tvSoundName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alert_sound, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = soundItems[position]

            holder.ivSoundIcon.setImageResource(item.iconRes)
            holder.tvSoundName.text = item.name

            // Show/hide badges
            holder.ivHotTag.visibility = if (item.hasHot) View.VISIBLE else View.GONE
            holder.tvAdTag.visibility = if (item.hasAd) View.VISIBLE else View.GONE

            // Selected state
            val isSelected = position == selectedSoundIndex
            holder.flSoundIconContainer.setBackgroundResource(
                when {
                    isSelected -> R.drawable.bg_sound_item_selected_ring
                    item.hasHot -> R.drawable.bg_sound_item_hot_ring
                    else -> 0
                }
            )
            holder.tvSoundName.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    if (isSelected) R.color.main_purple_dark else R.color.text_dark
                )
            )

            holder.itemView.setOnClickListener {
                val previousSelected = selectedSoundIndex
                selectedSoundIndex = holder.bindingAdapterPosition
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedSoundIndex)
                playPreviewSound(soundItems[selectedSoundIndex].rawResId)
            }
        }

        override fun getItemCount(): Int = soundItems.size
    }

    private fun playPreviewSound(resId: Int) {
        stopPreviewSound()
        try {
            // 使用 STREAM_ALARM 播放预览，与 DeviceAlertManager 保持一致
            val maxMusicVol = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val maxAlarmVol = audioMgr.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val alarmVol = if (maxMusicVol > 0) {
                ((currentVolume.toFloat() / maxMusicVol) * maxAlarmVol).toInt()
                    .coerceIn(if (currentVolume > 0) 1 else 0, maxAlarmVol)
            } else {
                maxAlarmVol
            }
            audioMgr.setStreamVolume(AudioManager.STREAM_ALARM, alarmVol, 0)

            val attributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            previewPlayer = MediaPlayer().apply {
                setAudioAttributes(attributes)
                resources.openRawResourceFd(resId)?.use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                setOnCompletionListener { release() }
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPreviewSound() {
        try {
            previewPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        previewPlayer = null
    }

    private fun loadNativeAd() {
        Log.d("AlertSoundActivity", "loadNativeAd() called, container visibility=${adBannerContainer.visibility}")
        lifecycleScope.launch {
            try {
                val maxRetries = 3
                for (attempt in 1..maxRetries) {
                    Log.d("AlertSoundActivity", "showNativeAdInContainer attempt $attempt/$maxRetries")
                    val success = AdShowExt.showNativeAdInContainer(this@AlertSoundActivity, adBannerContainer)
                    Log.d("AlertSoundActivity", "showNativeAdInContainer result: $success")
                    if (success) {
                        adBannerContainer.visibility = View.VISIBLE
                        return@launch
                    }
                    if (attempt < maxRetries) {
                        Log.d("AlertSoundActivity", "Retrying after 3s delay...")
                        kotlinx.coroutines.delay(3000)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlertSoundActivity", "loadNativeAd failed", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AudioDetectionService.pauseDetection(this)
    }

    override fun onPause() {
        super.onPause()
        AudioDetectionService.resumeDetection(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPreviewSound()
    }
}
