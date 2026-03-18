package com.mobile.clap.dev.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.clap.dev.R

class MicPermissionDialog : BottomSheetDialogFragment() {

    private var onGotItListener: (() -> Unit)? = null
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
        return inflater.inflate(R.layout.dialog_mic_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvDesc = view.findViewById<TextView>(R.id.tvMicDesc)
        val btnGotIt = view.findViewById<TextView>(R.id.btnGotIt)
        val ivCancel = view.findViewById<ImageView>(R.id.ivCancel)

        tvDesc.text = buildDescriptionText()

        btnGotIt.setOnClickListener {
            onGotItListener?.invoke()
            dismiss()
        }

        ivCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }

    private fun buildDescriptionText(): SpannableString {
        val full = getString(R.string.mic_dialog_desc)
        val spannable = SpannableString(full)

        val darkColor = ContextCompat.getColor(requireContext(), R.color.text_dark)

        // "not using your phone," — dark color only, no bold
        val darkOnlyPart = getString(R.string.mic_dialog_desc_highlight1)
        val darkStart = full.indexOf(darkOnlyPart)
        if (darkStart >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(darkColor),
                darkStart, darkStart + darkOnlyPart.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Bold + dark parts
        val boldDarkParts = listOf(
            getString(R.string.mic_dialog_desc_bold1),
            getString(R.string.mic_dialog_desc_bold2)
        )
        for (part in boldDarkParts) {
            val start = full.indexOf(part)
            if (start >= 0) {
                val end = start + part.length
                spannable.setSpan(
                    ForegroundColorSpan(darkColor),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannable
    }

    fun setOnGotItListener(listener: () -> Unit): MicPermissionDialog {
        onGotItListener = listener
        return this
    }

    fun setOnCancelListener(listener: () -> Unit): MicPermissionDialog {
        onCancelListener = listener
        return this
    }

    companion object {
        const val TAG = "MicPermissionDialog"

        fun newInstance(): MicPermissionDialog {
            return MicPermissionDialog()
        }
    }
}
