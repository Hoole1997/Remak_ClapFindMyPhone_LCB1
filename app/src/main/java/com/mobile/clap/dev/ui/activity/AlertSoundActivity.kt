package com.mobile.clap.dev.ui.activity

import android.graphics.Rect
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
import com.mobile.clap.dev.R

class AlertSoundActivity : AppCompatActivity() {

    private lateinit var seekBarVolume: SeekBar
    private lateinit var tvVolumePercent: TextView
    private lateinit var rvSounds: RecyclerView
    private lateinit var btnSave: TextView

    private var selectedSoundIndex = 3 // Cat selected by default

    private val soundItems = listOf(
        SoundItem("LABUBU", R.mipmap.img_setting_labubu, hasHot = true, hasAd = true),
        SoundItem("APT", R.mipmap.img_setting_apt, hasAd = true),
        SoundItem("Music", R.mipmap.img_setting_music, hasAd = true),
        SoundItem("Cat", R.mipmap.img_setting_cat),
        SoundItem("Dog", R.mipmap.img_setting_dog),
        SoundItem("Alarm", R.mipmap.img_setting_alarm),
        SoundItem("Hello", R.mipmap.img_setting_hello),
        SoundItem("Whistle", R.mipmap.img_setting_whistle),
        SoundItem("Gunshot", R.mipmap.img_setting_gunshot),
        SoundItem("Piano", R.mipmap.img_setting_piano),
        SoundItem("Train", R.mipmap.img_setting_train),
        SoundItem("Warning", R.mipmap.img_setting_warning)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert_sound)

        setupEdgeToEdge()
        initViews()
        setupVolumeSeekBar()
        setupSoundGrid()
        setupListeners()
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
    }

    private fun setupVolumeSeekBar() {
        tvVolumePercent.text = "${seekBarVolume.progress}%"
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvVolumePercent.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
            // TODO: Save selected sound and volume
            finish()
        }
    }

    data class SoundItem(
        val name: String,
        val iconRes: Int,
        val hasHot: Boolean = false,
        val hasAd: Boolean = false
    )

    private inner class SoundAdapter : RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val flSoundIconContainer: FrameLayout = itemView.findViewById(R.id.flSoundIconContainer)
            val ivSoundIcon: ImageView = itemView.findViewById(R.id.ivSoundIcon)
            val ivHotTag: ImageView = itemView.findViewById(R.id.ivHotTag)
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

            // Selected state
            val isSelected = position == selectedSoundIndex
            holder.flSoundIconContainer.setBackgroundResource(
                if (isSelected) R.drawable.bg_sound_item_selected_ring else 0
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
                // TODO: Play sound preview
            }
        }

        override fun getItemCount(): Int = soundItems.size
    }
}
