package com.mobile.clap.dev.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.common.bill.ads.renderer.AdmobNativeAdRenderer
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.mobile.clap.dev.R

class MyAdmobNativeAdRenderer : AdmobNativeAdRenderer {

    override fun createLayout(context: Context): NativeAdView {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_native_ad_admob, null, false) as NativeAdView
    }

    override fun bindData(adView: NativeAdView, nativeAd: NativeAd) {
        val ivIcon = adView.findViewById<ImageView>(R.id.iv_ad_icon)
        val tvHeadline = adView.findViewById<TextView>(R.id.tv_ad_headline)
        val tvBody = adView.findViewById<TextView>(R.id.tv_ad_body)
        val tvCta = adView.findViewById<TextView>(R.id.tv_ad_call_to_action)

        tvHeadline.text = nativeAd.headline
        tvBody.text = nativeAd.body
        tvCta.text = nativeAd.callToAction

        nativeAd.icon?.drawable?.let { ivIcon.setImageDrawable(it) }

        adView.headlineView = tvHeadline
        adView.bodyView = tvBody
        adView.callToActionView = tvCta
        adView.iconView = ivIcon

        adView.registerNativeAd(nativeAd, null)
    }
}
