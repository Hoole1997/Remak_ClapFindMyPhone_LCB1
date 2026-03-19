package com.mobile.clap.dev.ad

import android.content.Context
import com.android.common.bill.ads.renderer.MaxFullScreenNativeAdRenderer
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.mobile.clap.dev.R

class MyMaxFullScreenNativeAdRenderer : MaxFullScreenNativeAdRenderer {

    override fun createNativeAdView(context: Context): MaxNativeAdView {
        val binder = MaxNativeAdViewBinder.Builder(R.layout.layout_full_native_ad_max)
            .setTitleTextViewId(R.id.tv_ad_title)
            .setBodyTextViewId(R.id.tv_ad_body)
            .setCallToActionButtonId(R.id.tv_ad_call_to_action)
            .setIconImageViewId(R.id.iv_ad_icon)
            .setMediaContentViewGroupId(R.id.fl_ad_media)
            .build()
        return MaxNativeAdView(binder, context)
    }
}
