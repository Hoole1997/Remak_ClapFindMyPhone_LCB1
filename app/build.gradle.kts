plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val configSetting = findProperty("setting") as Map<*, *>
val link = findProperty("link") as Map<*, *>
val adMobConfig = findProperty("admob") as? Map<*, *> ?: emptyMap<Any, Any>()
val adMobUnitConfig = adMobConfig["adUnitIds"] as? Map<*, *> ?: emptyMap<Any, Any>()
val pangleConfig = findProperty("pangle") as? Map<*, *> ?: emptyMap<Any, Any>()
val pangleUnitConfig = pangleConfig["adUnitIds"] as? Map<*, *> ?: emptyMap<Any, Any>()
val toponConfig = findProperty("topon") as? Map<*, *> ?: emptyMap<Any, Any>()
val toponUnitConfig = toponConfig["adUnitIds"] as? Map<*, *> ?: emptyMap<Any, Any>()
val maxConfig = findProperty("max") as? Map<*, *> ?: emptyMap<Any, Any>()
val maxUnitConfig = maxConfig["adUnitIds"] as? Map<*, *> ?: emptyMap<Any, Any>()
val analyticsConfig = findProperty("analytics") as? Map<*, *> ?: emptyMap<Any, Any>()

android {
    namespace = "com.mobile.clap.dev"
    compileSdk = 36

    defaultConfig {
        minSdk = configSetting["minSdk"] as Int
        targetSdk = configSetting["targetSdk"] as Int
        versionCode = configSetting["versionCode"] as Int
        versionName = configSetting["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PRIVACY_POLICY", "\"${link["privacyUrl"]}\"")
        buildConfigField("String", "FCM_LINK", "\"${link["fcmLink"]}\"")
        buildConfigField("String", "DEFAULT_USER_CHANNEL", "\"${analyticsConfig["defaultUserChannel"] ?: "natural"}\"")

        // AdMob
        buildConfigField("String", "ADMOB_APPLICATION_ID", "\"${adMobConfig["applicationId"] ?: ""}\"")
        buildConfigField("String", "ADMOB_SPLASH_ID", "\"${adMobUnitConfig["splash"] ?: ""}\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${adMobUnitConfig["banner"] ?: ""}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${adMobUnitConfig["interstitial"] ?: ""}\"")
        buildConfigField("String", "ADMOB_NATIVE_ID", "\"${adMobUnitConfig["native"] ?: ""}\"")
        buildConfigField("String", "ADMOB_FULL_NATIVE_ID", "\"${adMobUnitConfig["full_native"] ?: ""}\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"${adMobUnitConfig["rewarded"] ?: ""}\"")

        // Pangle
        buildConfigField("String", "PANGLE_APPLICATION_ID", "\"${pangleConfig["applicationId"] ?: ""}\"")
        buildConfigField("String", "PANGLE_SPLASH_ID", "\"${pangleUnitConfig["splash"] ?: ""}\"")
        buildConfigField("String", "PANGLE_BANNER_ID", "\"${pangleUnitConfig["banner"] ?: ""}\"")
        buildConfigField("String", "PANGLE_INTERSTITIAL_ID", "\"${pangleUnitConfig["interstitial"] ?: ""}\"")
        buildConfigField("String", "PANGLE_NATIVE_ID", "\"${pangleUnitConfig["native"] ?: ""}\"")
        buildConfigField("String", "PANGLE_FULL_NATIVE_ID", "\"${pangleUnitConfig["full_native"] ?: ""}\"")
        buildConfigField("String", "PANGLE_REWARDED_ID", "\"${pangleUnitConfig["rewarded"] ?: ""}\"")

        // TopOn
        buildConfigField("String", "TOPON_APPLICATION_ID", "\"${toponConfig["applicationId"] ?: ""}\"")
        buildConfigField("String", "TOPON_APP_KEY", "\"${toponConfig["appKey"] ?: ""}\"")
        buildConfigField("String", "TOPON_SPLASH_ID", "\"${toponUnitConfig["splash"] ?: ""}\"")
        buildConfigField("String", "TOPON_BANNER_ID", "\"${toponUnitConfig["banner"] ?: ""}\"")
        buildConfigField("String", "TOPON_INTERSTITIAL_ID", "\"${toponUnitConfig["interstitial"] ?: ""}\"")
        buildConfigField("String", "TOPON_NATIVE_ID", "\"${toponUnitConfig["native"] ?: ""}\"")
        buildConfigField("String", "TOPON_FULL_NATIVE_ID", "\"${toponUnitConfig["full_native"] ?: ""}\"")
        buildConfigField("String", "TOPON_REWARDED_ID", "\"${toponUnitConfig["rewarded"] ?: ""}\"")

        // MAX
        buildConfigField("String", "MAX_SDK_KEY", "\"${maxConfig["sdkKey"] ?: ""}\"")
        buildConfigField("String", "MAX_SPLASH_ID", "\"${maxUnitConfig["splash"] ?: ""}\"")
        buildConfigField("String", "MAX_BANNER_ID", "\"${maxUnitConfig["banner"] ?: ""}\"")
        buildConfigField("String", "MAX_INTERSTITIAL_ID", "\"${maxUnitConfig["interstitial"] ?: ""}\"")
        buildConfigField("String", "MAX_NATIVE_ID", "\"${maxUnitConfig["native"] ?: ""}\"")
        buildConfigField("String", "MAX_FULL_NATIVE_ID", "\"${maxUnitConfig["fullNative"] ?: ""}\"")
        buildConfigField("String", "MAX_REWARDED_ID", "\"${maxUnitConfig["rewarded"] ?: ""}\"")

        manifestPlaceholders["ADMOB_APPLICATION_ID"] = adMobConfig["applicationId"]?.toString().orEmpty()

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }

        multiDexEnabled = true
    }

    // Flavor 配置
    flavorDimensions += "distribution"

    productFlavors {
        // 开发测试版本
        create("developer") {
            dimension = "distribution"
            applicationId = configSetting["applicationId"] as String
            versionNameSuffix = "-developer"
            isDefault = true
        }

        // 正式发布版本
        create("official") {
            dimension = "distribution"
            applicationId = configSetting["applicationId"] as String
            versionNameSuffix = "-official"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 设置APK输出文件名
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "ClapFindMyPhone - ${variant.baseName} - ${variant.versionName}.apk"
                output.outputFileName = outputFileName
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("com.github.toukaremax:core:1.0.9")
    implementation("com.github.toukaremax:bill:1.0.10")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}