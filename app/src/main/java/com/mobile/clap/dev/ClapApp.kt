package com.mobile.clap.dev

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.TextView
import com.android.common.bill.BillConfig
import com.android.common.bill.ads.PreloadController
import com.android.common.bill.ads.bidding.AppOpenBiddingInitializer
import com.android.common.bill.ads.log.AdLogger
import com.android.common.bill.ads.renderer.AdLoadingDialogRenderer
import com.mobile.clap.dev.ad.MyAdmobFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyAdmobNativeAdRenderer
import com.mobile.clap.dev.ad.MyMaxFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyMaxNativeAdRenderer
import com.mobile.clap.dev.ad.MyPangleFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyPangleNativeAdRenderer
import com.mobile.clap.dev.ad.MyToponFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyToponNativeAdRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.corekit.core.controller.ChannelUserController
import net.corekit.core.log.CoreLogger

class ClapApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        ChannelUserController.setDefaultChannel(BuildConfig.DEFAULT_USER_CHANNEL)
        CoreLogger.setLogEnabled(BuildConfig.DEBUG)
        AdLogger.setLogEnabled(BuildConfig.DEBUG)
    }

    override fun onCreate() {
        super.onCreate()
        initAdSDK()
    }

    private fun initAdSDK() {
        Log.d("ClapApp", "initAdSDK() starting...")
        applicationScope.launch {
            Log.d("ClapApp", "AppOpenBiddingInitializer.initialize() starting...")
            AppOpenBiddingInitializer.initialize(this@ClapApp, R.mipmap.ic_launcher) {
                admob = BillConfig.AdmobConfig(
                    applicationId = BuildConfig.ADMOB_APPLICATION_ID,
                    splashId = BuildConfig.ADMOB_SPLASH_ID,
                    bannerId = BuildConfig.ADMOB_BANNER_ID,
                    interstitialId = BuildConfig.ADMOB_INTERSTITIAL_ID,
                    nativeId = BuildConfig.ADMOB_NATIVE_ID,
                    fullNativeId = BuildConfig.ADMOB_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.ADMOB_REWARDED_ID
                )
                pangle = BillConfig.PangleConfig(
                    applicationId = BuildConfig.PANGLE_APPLICATION_ID,
                    splashId = BuildConfig.PANGLE_SPLASH_ID,
                    bannerId = BuildConfig.PANGLE_BANNER_ID,
                    interstitialId = BuildConfig.PANGLE_INTERSTITIAL_ID,
                    nativeId = BuildConfig.PANGLE_NATIVE_ID,
                    fullNativeId = BuildConfig.PANGLE_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.PANGLE_REWARDED_ID
                )
                topon = BillConfig.ToponConfig(
                    applicationId = BuildConfig.TOPON_APPLICATION_ID,
                    appKey = BuildConfig.TOPON_APP_KEY,
                    splashId = BuildConfig.TOPON_SPLASH_ID,
                    bannerId = BuildConfig.TOPON_BANNER_ID,
                    interstitialId = BuildConfig.TOPON_INTERSTITIAL_ID,
                    nativeId = BuildConfig.TOPON_NATIVE_ID,
                    fullNativeId = BuildConfig.TOPON_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.TOPON_REWARDED_ID
                )
                max = BillConfig.MaxConfig(
                    sdkKey = BuildConfig.MAX_SDK_KEY,
                    splashId = BuildConfig.MAX_SPLASH_ID,
                    bannerId = BuildConfig.MAX_BANNER_ID,
                    interstitialId = BuildConfig.MAX_INTERSTITIAL_ID,
                    nativeId = BuildConfig.MAX_NATIVE_ID,
                    fullNativeId = BuildConfig.MAX_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.MAX_REWARDED_ID
                )
                adLoadingDialogRenderer = object : AdLoadingDialogRenderer {
                    override fun getLayoutResId(): Int = R.layout.dialog_ad_loading

                    override fun onViewCreated(view: View, onReady: () -> Unit) {
                        onReady()
                    }

                    override fun updateText(view: View, text: String) {
                        view.findViewById<TextView>(R.id.tv_ad_loading)?.text = text
                    }

                    override fun onDestroy(view: View) = Unit
                }

                admobNativeRenderer = MyAdmobNativeAdRenderer()
                admobFullScreenNativeRenderer = MyAdmobFullScreenNativeAdRenderer()
                pangleNativeRenderer = MyPangleNativeAdRenderer()
                pangleFullScreenNativeRenderer = MyPangleFullScreenNativeAdRenderer()
                toponNativeRenderer = MyToponNativeAdRenderer()
                toponFullScreenNativeRenderer = MyToponFullScreenNativeAdRenderer()
                maxNativeRenderer = MyMaxNativeAdRenderer()
                maxFullScreenNativeRenderer = MyMaxFullScreenNativeAdRenderer()

            }
            Log.d("ClapApp", "AppOpenBiddingInitializer.initialize() completed, starting preload...")
            CoreLogger.setLogEnabled(BuildConfig.DEBUG)
            AdLogger.setLogEnabled(BuildConfig.DEBUG)
            PreloadController.preloadAll(this@ClapApp)
            Log.d("ClapApp", "PreloadController.preloadAll() completed")
        }
    }
}
