package com.mobile.clap.dev.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.android.common.bill.ads.renderer.AdmobFullScreenNativeAdRenderer
import com.mobile.clap.dev.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class MyAdmobFullScreenNativeAdRenderer : AdmobFullScreenNativeAdRenderer {

    override fun createLayout(context: Context): NativeAdView {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_full_native_ad_admob, null) as NativeAdView
    }

    override fun bindData(adView: NativeAdView, nativeAd: NativeAd, lifecycleOwner: LifecycleOwner) {
        val titleView = adView.findViewById<TextView>(R.id.tv_ad_title)
        val descView = adView.findViewById<TextView>(R.id.tv_ad_description)
        val ctaButton = adView.findViewById<TextView>(R.id.btn_ad_cta)
        val iconView = adView.findViewById<ImageView>(R.id.iv_ad_icon)
        val iconContainer = adView.findViewById<View>(R.id.iconCv)
        val mediaView = adView.findViewById<MediaView>(R.id.mv_ad_media)

        titleView.text = nativeAd.headline

        val body = nativeAd.body
        descView.text = body
        descView.visibility = if (body.isNullOrBlank()) View.GONE else View.VISIBLE

        val callToAction = nativeAd.callToAction
        ctaButton.text = callToAction
        ctaButton.visibility = if (callToAction.isNullOrBlank()) View.INVISIBLE else View.VISIBLE

        val icon = nativeAd.icon
        if (icon?.drawable != null) {
            iconView.setImageDrawable(icon.drawable)
            iconContainer.visibility = View.VISIBLE
        } else {
            iconView.setImageDrawable(null)
            iconContainer.visibility = View.GONE
        }

        adView.mediaView = mediaView
        val mediaContent = nativeAd.mediaContent
        if (mediaContent != null) {
            mediaView.mediaContent = mediaContent
            mediaView.visibility = View.VISIBLE
        } else {
            mediaView.visibility = View.GONE
        }

        adView.headlineView = titleView
        adView.bodyView = descView
        adView.callToActionView = ctaButton
        adView.iconView = iconView
        adView.starRatingView = null
        adView.advertiserView = null
        adView.priceView = null
        adView.storeView = null
        adView.setNativeAd(nativeAd)
    }

    override fun createLoadingView(context: Context, container: ViewGroup) {
        container.removeAllViews()
        val loadingView = LayoutInflater.from(context)
            .inflate(R.layout.layout_fullscreen_loading, container, false)
        container.addView(loadingView)
    }
}
