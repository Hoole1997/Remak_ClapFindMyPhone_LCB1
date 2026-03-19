# ==================== App 模块混淆规则 ====================

# 保留行号信息（Crashlytics 堆栈还原）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== MediaPipe Tasks Audio ====================

# MediaPipe 使用 JNI + 反射加载模型和处理器
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# MediaPipe 内部依赖的 protobuf-lite
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ==================== TFLite 模型文件 ====================

# YAMNet 模型通过 asset 路径字符串加载，确保 AssetFileDescriptor 相关不被优化
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# ==================== toukaremax SDK ====================

-keep class com.remax.** { *; }
-dontwarn com.remax.**
-keep class com.toukaremax.** { *; }
-dontwarn com.toukaremax.**

# ==================== App 自身 Parcelable ====================

# AudioDetectionConfig 通过 Intent extra 传递，Parcelize 生成的 CREATOR 必须保留
-keep class com.mobile.clap.dev.ml.AudioDetectionConfig { *; }
-keep class com.mobile.clap.dev.ml.AudioDetectionConfig$* { *; }

# ==================== RemoteViews (自定义通知布局) ====================

# 自定义通知布局中引用的 View ID 通过 RemoteViews 反射访问
-keep class android.widget.RemoteViews { *; }

# ==================== utilcodex ====================

-keep class com.blankj.utilcode.** { *; }
-dontwarn com.blankj.utilcode.**

# ==================== Glide ====================

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# ==================== Crashlytics 符号映射 ====================

-keepattributes *Annotation*