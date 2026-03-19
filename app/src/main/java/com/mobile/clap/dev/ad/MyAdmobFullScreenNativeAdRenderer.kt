package com.mobile.clap.dev.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.android.common.bill.ads.renderer.AdmobFullScreenNativeAdRenderer
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.mobile.clap.dev.R

class MyAdmobFullScreenNativeAdRenderer : AdmobFullScreenNativeAdRenderer {

    override fun createLayout(context: Context): NativeAdView {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_full_native_ad_admob, null, false) as NativeAdView
    }

    override fun bindData(adView: NativeAdView, nativeAd: NativeAd, lifecycleOwner: LifecycleOwner) {
        val ivIcon = adView.findViewById<ImageView>(R.id.iv_ad_icon)
        val tvHeadline = adView.findViewById<TextView>(R.id.tv_ad_headline)
        val tvBody = adView.findViewById<TextView>(R.id.tv_ad_body)
        val tvCta = adView.findViewById<TextView>(R.id.tv_ad_call_to_action)
        val mediaView = adView.findViewById<MediaView>(R.id.media_view)

        tvHeadline.text = nativeAd.headline
        tvBody.text = nativeAd.body
        tvCta.text = nativeAd.callToAction

        nativeAd.icon?.drawable?.let { ivIcon.setImageDrawable(it) }

        adView.headlineView = tvHeadline
        adView.bodyView = tvBody
        adView.callToActionView = tvCta
        adView.iconView = ivIcon

        adView.registerNativeAd(nativeAd, mediaView)
    }

    override fun createLoadingView(context: Context, container: ViewGroup) {
        container.removeAllViews()
        val progressBar = ProgressBar(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(progressBar)
    }
}
