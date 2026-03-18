package com.mobile.clap.dev.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.clap.dev.R

class TestMicDialog : BottomSheetDialogFragment() {

    private var onLaterListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null

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
        return inflater.inflate(R.layout.dialog_test_mic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvDesc = view.findViewById<TextView>(R.id.tvTestMicDesc)
        val btnLater = view.findViewById<TextView>(R.id.btnTestLater)
        val ivCancel = view.findViewById<ImageView>(R.id.ivCancel)

        tvDesc.text = buildDescriptionText()

        btnLater.paintFlags = btnLater.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        btnLater.setOnClickListener {
            onLaterListener?.invoke()
            dismiss()
        }

        ivCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }

    private fun buildDescriptionText(): SpannableString {
        val full = getString(R.string.test_mic_dialog_desc)
        val spannable = SpannableString(full)

        val darkColor = ContextCompat.getColor(requireContext(), R.color.text_dark)

        // "Clap your hands " — bold + dark
        val boldPart = getString(R.string.test_mic_dialog_desc_bold)
        val boldStart = full.indexOf(boldPart)
        if (boldStart >= 0) {
            val boldEnd = boldStart + boldPart.length
            spannable.setSpan(
                ForegroundColorSpan(darkColor),
                boldStart, boldEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                boldStart, boldEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // "or whistle" — dark color only, no bold
        val darkPart = getString(R.string.test_mic_dialog_desc_dark)
        val darkStart = full.indexOf(darkPart)
        if (darkStart >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(darkColor),
                darkStart, darkStart + darkPart.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }

    fun setOnLaterListener(listener: () -> Unit): TestMicDialog {
        onLaterListener = listener
        return this
    }

    fun setOnCancelListener(listener: () -> Unit): TestMicDialog {
        onCancelListener = listener
        return this
    }

    companion object {
        const val TAG = "TestMicDialog"

        fun newInstance(): TestMicDialog {
            return TestMicDialog()
        }
    }
}
