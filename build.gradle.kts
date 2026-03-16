// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.room) apply false
}

// 根据构建任务自动选择配置文件
val taskNames = gradle.startParameter.taskNames
val configFile = when {
    taskNames.any {
        it.contains(
            "official",
            ignoreCase = true
        )
    } -> file("app/src/official/configSetting.gradle")

    taskNames.any {
        it.contains(
            "developer",
            ignoreCase = true
        )
    } -> file("app/src/developer/configSetting.gradle")

    else -> file("app/src/developer/configSetting.gradle") // 默认使用开发配置
}

apply {
    from(configFile)
}