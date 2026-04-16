plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("kotlin-parcelize")
}

fun secretValue(name: String): String {
    return (findProperty(name) as? String).orEmpty().ifBlank {
        System.getenv(name).orEmpty()
    }
}

fun resolveSigningFile(path: String): File {
    val configuredFile = File(path)
    if (configuredFile.isAbsolute) {
        return configuredFile
    }

    val rootRelativeFile = rootProject.file(path)
    if (rootRelativeFile.exists()) {
        return rootRelativeFile
    }

    return file(path.removePrefix("app/"))
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
val officialReleaseKeystorePath = secretValue("ANDROID_SIGNING_STORE_FILE").ifBlank {
    "app/src/official/official-release.keystore"
}
val officialReleaseKeystoreFile = resolveSigningFile(officialReleaseKeystorePath)
val officialReleaseStorePassword = secretValue("ANDROID_SIGNING_STORE_PASSWORD").ifBlank {
    "official123456"
}
val officialReleaseKeyAlias = secretValue("ANDROID_SIGNING_KEY_ALIAS").ifBlank {
    "official"
}
val officialReleaseKeyPassword = secretValue("ANDROID_SIGNING_KEY_PASSWORD").ifBlank {
    "official123456"
}
val hasOfficialReleaseSigning = officialReleaseKeystoreFile.isFile &&
        officialReleaseKeystoreFile.length() > 0L &&
        officialReleaseStorePassword.isNotBlank() &&
        officialReleaseKeyAlias.isNotBlank() &&
        officialReleaseKeyPassword.isNotBlank()
val requiresOfficialReleaseSigning = gradle.startParameter.taskNames.any {
    it.contains("official", ignoreCase = true) && it.contains("release", ignoreCase = true)
}

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
            abiFilters.addAll(listOf("arm64-v8a"))
        }

        multiDexEnabled = true
    }

    signingConfigs {
        create("officialRelease") {
            if (hasOfficialReleaseSigning) {
                storeFile = officialReleaseKeystoreFile
                storePassword = officialReleaseStorePassword
                keyAlias = officialReleaseKeyAlias
                keyPassword = officialReleaseKeyPassword
            }
        }
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
        debug {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            check(hasOfficialReleaseSigning || !requiresOfficialReleaseSigning) {
                "Missing official release signing config. Ensure app/src/official/official-release.keystore exists or set ANDROID_SIGNING_STORE_FILE, ANDROID_SIGNING_STORE_PASSWORD, ANDROID_SIGNING_KEY_ALIAS, and ANDROID_SIGNING_KEY_PASSWORD."
            }
            signingConfig = signingConfigs.getByName("officialRelease")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    // 设置APK输出文件名
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "LCB_ClapFindMyPhone2 - ${variant.baseName} - ${variant.versionName}.apk"
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
    packaging {
        jniLibs {
            // The launcher SDK ships 360 Jiagu protected native loaders and
            // expects native libraries to be extracted before class bootstrap.
            useLegacyPackaging = true
        }
    }
    buildFeatures {
        buildConfig = true
    }

    // Don't compress tflite model files
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    implementation(project(":analytics"))
    implementation(project(":base"))
    implementation(project(":bill"))
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // MediaPipe Tasks Audio for ML audio classification
    implementation("com.google.mediapipe:tasks-audio:0.10.32")

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
