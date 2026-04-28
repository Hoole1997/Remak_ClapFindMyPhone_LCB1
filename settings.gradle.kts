import java.util.Properties

pluginManagement {
    plugins {
        id("com.android.settings") version "8.13.0"
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.android.settings")
}

android {
    execution {
        profiles {
            create("default") {
                r8.runInSeparateProcess = false
            }
            create("lowMemory") {
                r8 {
                    runInSeparateProcess = true
                    jvmOptions += listOf(
                        "-Xms256m",
                        "-Xmx6g",
                        "-XX:MaxMetaspaceSize=1g",
                        "-Dfile.encoding=UTF-8",
                    )
                }
            }
            defaultProfile = "default"
        }
    }
}

val buildConfigFile = file("build.config.properties")
val buildConfig = Properties()
if (buildConfigFile.exists()) {
    buildConfig.load(buildConfigFile.inputStream())
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Pangle SDK
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")
            content {
                includeGroup("com.pangle.global")
            }
        }
        // TopOn SDK
        maven {
            url = uri("https://jfrog.anythinktech.com/artifactory/overseas_sdk")
            content {
                includeGroup("com.thinkup.sdk")
                includeGroup("com.smartdigimkttech.sdk")
            }
        }
        // Mintegral SDK
        maven {
            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
            content {
                includeGroup("com.mbridge.msdk.oversea")
            }
        }
        // IronSource SDK
        maven {
            url = uri("https://android-sdk.is.com/")
            content {
                includeGroup("com.ironsource.sdk")
            }
        }
        // Bigo Ads SDK
        maven {
            url = uri("https://api.ad.bigossp.com/repository/maven-public/")
            content {
                includeGroup("com.bigossp")
            }
        }
        // Vungle SDK
        maven {
            url = uri("https://sdk.vungle.com/public/")
            content {
                includeGroup("com.vungle")
            }
        }
        maven {
            url = uri("https://artifacts.applovin.com/android")
        }
        maven("https://cboost.jfrog.io/artifactory/chartboost-ads")
        maven("https://repo.dgtverse.cn/repository/maven-public")
        maven {
            url = uri("https://maven.pkg.github.com/toukaRemax/remax_sdk")
            credentials {
                username = buildConfig.getProperty("github.user") ?: System.getenv("GITHUB_ACTOR")
                password = buildConfig.getProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "LCB_ClapFindMyPhone_variant_2"
include(":app")
include(":base")
include(":analytics")
//include(":core")
//include(":bill")
