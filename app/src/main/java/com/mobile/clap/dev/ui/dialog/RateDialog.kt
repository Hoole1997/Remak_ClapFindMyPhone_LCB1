package com.mobile.clap.dev.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.clap.dev.R

class RateDialog : BottomSheetDialogFragment() {

    private var currentRating = 0
    private var onSubmitListener: ((Int) -> Unit)? = null
    private var onLaterListener: (() -> Unit)? = null

    private lateinit var stars: List<ImageView>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
        return inflater.inflate(R.layout.dialog_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvRateTitle)
        val btnSubmit = view.findViewById<TextView>(R.id.btnSubmit)
        val btnLater = view.findViewById<TextView>(R.id.btnLater)

        val appName = requireContext().getString(R.string.app_name)
        tvTitle.text = getString(R.string.rate_dialog_title, appName)

        stars = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                setRating(index + 1)
            }
        }

        btnSubmit.setOnClickListener {
            onSubmitListener?.invoke(currentRating)
            dismiss()
        }

        btnLater.setOnClickListener {
            onLaterListener?.invoke()
            dismiss()
        }
    }

    private fun setRating(rating: Int) {
        currentRating = rating
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.svg_start_selected)
            } else {
                imageView.setImageResource(R.drawable.svg_start_unselect)
            }
        }
    }

    fun setOnSubmitListener(listener: (Int) -> Unit): RateDialog {
        onSubmitListener = listener
        return this
    }

    fun setOnLaterListener(listener: () -> Unit): RateDialog {
        onLaterListener = listener
        return this
    }

    companion object {
        const val TAG = "RateDialog"

        fun newInstance(): RateDialog {
            return RateDialog()
        }
    }
}
