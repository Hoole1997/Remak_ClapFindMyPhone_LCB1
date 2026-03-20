package com.mobile.clap.dev.ml

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.mobile.clap.dev.R

/**
 * 警报音效资源管理类
 * 统一管理所有警报音效的名称、图标和音频资源
 */
object AlertSoundResources {

    /**
     * 音效数据类
     */
    data class AlertSound(
        val index: Int,
        val displayName: String,
        @DrawableRes val iconResId: Int,
        @RawRes val audioResId: Int,
        val isHot: Boolean = false
    )

    /**
     * 所有音效列表
     * 顺序: LABUBU, APT, Music, Cat, Dog, Alarm, Hello, Whistle, Gunshot, Piano, Train, Warning
     *
     * NOTE: 需要在 app/src/main/res/raw/ 下添加对应音频文件:
     *   alert_labubu, alert_apt, alert_music, alert_cat, alert_dog,
     *   alert_alarm, alert_bell, alert_whistle, alert_gunshot,
     *   alert_piano, alert_train, alert_warning
     */
    val soundList: List<AlertSound> = listOf(
        AlertSound(0, "LABUBU", R.mipmap.img_setting_labubu, R.raw.alert_labubu, isHot = true),
        AlertSound(1, "APT", R.mipmap.img_setting_apt, R.raw.alert_apt),
        AlertSound(2, "Music", R.mipmap.img_setting_music, R.raw.alert_music),
        AlertSound(3, "Cat", R.mipmap.img_setting_cat, R.raw.alert_cat),
        AlertSound(4, "Dog", R.mipmap.img_setting_dog, R.raw.alert_dog),
        AlertSound(5, "Alarm", R.mipmap.img_setting_alarm, R.raw.alert_waring),
        AlertSound(6, "Hello", R.mipmap.img_setting_hello, R.raw.alert_whistle),
        AlertSound(7, "Whistle", R.mipmap.img_setting_whistle, R.raw.alert_bell),
        AlertSound(8, "Gunshot", R.mipmap.img_setting_gunshot, R.raw.alert_gunshot),
        AlertSound(9, "Piano", R.mipmap.img_setting_piano, R.raw.alert_piano),
        AlertSound(10, "Train", R.mipmap.img_setting_train, R.raw.alert_train),
        AlertSound(11, "Warning", R.mipmap.img_setting_warning, R.raw.alert_alarm),
    )

    /**
     * 音效数量
     */
    val totalCount: Int get() = soundList.size

    /**
     * 根据索引获取音效音频资源ID
     * @param index 音效索引
     * @return 音效raw资源ID
     */
    @RawRes
    fun getAudioResId(index: Int): Int {
        return soundList.getOrNull(index)?.audioResId ?: soundList[0].audioResId
    }

    /**
     * 根据索引获取音效显示名称
     * @param index 音效索引
     * @return 音效名称
     */
    fun getDisplayName(index: Int): String {
        return soundList.getOrNull(index)?.displayName ?: soundList[0].displayName
    }

    /**
     * 根据索引获取音效图标资源ID
     * @param index 音效索引
     * @return 音效图标资源ID
     */
    @DrawableRes
    fun getIconResId(index: Int): Int {
        return soundList.getOrNull(index)?.iconResId ?: soundList[0].iconResId
    }

    /**
     * 根据索引获取音效项
     * @param index 音效索引
     * @return 音效项
     */
    fun getItem(index: Int): AlertSound {
        return soundList.getOrNull(index) ?: soundList[0]
    }
}
