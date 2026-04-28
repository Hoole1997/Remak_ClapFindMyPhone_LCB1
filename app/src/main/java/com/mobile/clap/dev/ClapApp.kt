package com.mobile.clap.dev

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.common.bill.BillConfig
import com.android.common.bill.ads.PreloadController
import com.android.common.bill.ads.bidding.AppOpenBiddingInitializer
import com.android.common.bill.ads.log.AdLogger
import com.android.common.bill.ads.renderer.AdLoadingDialogRenderer
import com.blankj.utilcode.util.LogUtils
import com.mobile.clap.dev.ad.DefaultGamFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.DefaultGamNativeAdRenderer
import com.mobile.clap.dev.ad.MyAdmobFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyAdmobNativeAdRenderer
import com.mobile.clap.dev.ad.MyPangleFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyPangleNativeAdRenderer
import com.mobile.clap.dev.ad.MyToponFullScreenNativeAdRenderer
import com.mobile.clap.dev.ad.MyToponNativeAdRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.corekit.core.controller.ChannelUserController
import com.mobile.clap.dev.analytics.CoreSdkTrackerBridge
import com.mobile.clap.dev.ui.activity.AlertSoundActivity
import com.mobile.clap.dev.ui.activity.ClapSplashActivity
import com.mobile.clap.dev.ui.activity.DebugActivity
import com.mobile.clap.dev.ui.activity.MainActivity
import com.remax.analytics.adjust.AdjustController
import net.corekit.core.log.CoreLogger

class ClapApp : com.find.your.phone.by.clap.tool.Rbs6d4cptydhri() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        var clapApp: ClapApp?=null
    }

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        ChannelUserController.setDefaultChannel(BuildConfig.DEFAULT_USER_CHANNEL)
        CoreLogger.setLogEnabled(BuildConfig.DEBUG)
        AdLogger.setLogEnabled(BuildConfig.DEBUG)
    }

    override fun onCreate() {
        super.onCreate()
        clapApp = this
        CoreSdkTrackerBridge.initialize()
        this.smartcleancorewifi {isOrganic, network, campaign, adgroup, creative, jsonResponse ->
            AdjustController.initialize(
                context = applicationContext,
                network = network,
                campaign = campaign,
                adgroup = adgroup,
                creative = creative,
                jsonResponse = jsonResponse
            )
            LogUtils.i("onCreate: isOrganic = $isOrganic , network = $network , campaign = $campaign , adgroup = $adgroup , creative = $creative , jsonResponse = $jsonResponse")
        }
        initAdSDK()
    }

    private fun initAdSDK() {
        Log.d("ClapApp", "initAdSDK() starting...")
        applicationScope.launch {
            Log.d("ClapApp", "AppOpenBiddingInitializer.initialize() starting...")
            AppOpenBiddingInitializer.initialize(this@ClapApp, R.mipmap.ic_launcher) {
                googleMobileAds = BillConfig.GoogleMobileAdsConfig(
                    applicationId = BuildConfig.ADMOB_APPLICATION_ID
                )
                admob = BillConfig.AdmobConfig(
                    splashId = BuildConfig.ADMOB_SPLASH_ID,
                    bannerId = BuildConfig.ADMOB_BANNER_ID,
                    interstitialId = BuildConfig.ADMOB_INTERSTITIAL_ID,
                    nativeId = BuildConfig.ADMOB_NATIVE_ID,
                    fullNativeId = BuildConfig.ADMOB_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.ADMOB_REWARDED_ID
                )
                gam = BillConfig.GamConfig(
                    splashId = BuildConfig.GAM_SPLASH_ID,
                    bannerId = BuildConfig.GAM_BANNER_ID,
                    interstitialId = BuildConfig.GAM_INTERSTITIAL_ID,
                    nativeId = BuildConfig.GAM_NATIVE_ID,
                    fullNativeId = BuildConfig.GAM_FULL_NATIVE_ID,
                    rewardedId = BuildConfig.GAM_REWARDED_ID
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

                adLoadingDialogRenderer = object : AdLoadingDialogRenderer {
                    override fun getLayoutResId(): Int = R.layout.dialog_ad_loading

                    override fun onViewCreated(view: View, onReady: () -> Unit) {
                        onReady()
                    }

                    override fun updateText(view: View, text: String) {
                        view.findViewById<TextView>(R.id.tv_ad_loading)?.text = text
                    }

                    override fun findCloseView(view: View): View? {
                        return view.findViewById<ImageView>(R.id.iv_close)
                    }

                    override fun onDestroy(view: View) = Unit
                }

                admobNativeRenderer = MyAdmobNativeAdRenderer()
                admobFullScreenNativeRenderer = MyAdmobFullScreenNativeAdRenderer()
                gamNativeRenderer = DefaultGamNativeAdRenderer()
                gamFullScreenNativeRenderer = DefaultGamFullScreenNativeAdRenderer()
                pangleNativeRenderer = MyPangleNativeAdRenderer()
                pangleFullScreenNativeRenderer = MyPangleFullScreenNativeAdRenderer()
                toponNativeRenderer = MyToponNativeAdRenderer()
                toponFullScreenNativeRenderer = MyToponFullScreenNativeAdRenderer()
            }
            Log.d("ClapApp", "AppOpenBiddingInitializer.initialize() completed, starting preload...")
            CoreLogger.setLogEnabled(BuildConfig.DEBUG)
            AdLogger.setLogEnabled(BuildConfig.DEBUG)
            PreloadController.preloadAll(this@ClapApp)
            Log.d("ClapApp", "PreloadController.preloadAll() completed")
        }
    }

    override fun autosecureprovault(): Class<in Any>? {
        return ClapSplashActivity::class.java as Class<in Any>?
    }

    override fun scanfastcorememory(): List<Class<in Any>?>? {
        return listOf(
            ClapSplashActivity::class.java,
            MainActivity::class.java,
            AlertSoundActivity::class.java,
            DebugActivity::class.java,
        ) as List<Class<in Any>?>?
    }
}
