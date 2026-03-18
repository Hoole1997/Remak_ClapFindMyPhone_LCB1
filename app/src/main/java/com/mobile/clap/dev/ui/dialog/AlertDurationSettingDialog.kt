package com.mobile.clap.dev.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.clap.dev.R

class AlertDurationSettingDialog : BottomSheetDialogFragment() {

    private var selectedDuration: Int = DEFAULT_DURATION
    private var onConfirmListener: ((Int) -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null

    private lateinit var optionViews: List<TextView>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.setBackgroundColor(Color.TRANSPARENT)
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_alert_duration_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOk = view.findViewById<TextView>(R.id.btnOk)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        optionViews = listOf(
            view.findViewById(R.id.tvOption10),
            view.findViewById(R.id.tvOption30),
            view.findViewById(R.id.tvOption60),
            view.findViewById(R.id.tvOption120)
        )

        optionViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                selectedDuration = DURATIONS[index]
                updateSelection()
            }
        }

        updateSelection()

        btnOk.setOnClickListener {
            onConfirmListener?.invoke(selectedDuration)
            dismiss()
        }

        btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }

    private fun updateSelection() {
        val purpleColor = ContextCompat.getColor(requireContext(), R.color.main_purple_dark)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.text_medium)

        optionViews.forEachIndexed { index, tv ->
            if (DURATIONS[index] == selectedDuration) {
                tv.setTextColor(purpleColor)
            } else {
                tv.setTextColor(defaultColor)
            }
        }
    }

    fun setSelectedDuration(duration: Int): AlertDurationSettingDialog {
        selectedDuration = duration
        return this
    }

    fun setOnConfirmListener(listener: (Int) -> Unit): AlertDurationSettingDialog {
        onConfirmListener = listener
        return this
    }

    fun setOnCancelListener(listener: () -> Unit): AlertDurationSettingDialog {
        onCancelListener = listener
        return this
    }

    companion object {
        const val TAG = "AlertDurationSettingDialog"
        private const val DEFAULT_DURATION = 10
        val DURATIONS = listOf(10, 30, 60, 120)

        fun newInstance(): AlertDurationSettingDialog {
            return AlertDurationSettingDialog()
        }
    }
}
