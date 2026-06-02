package com.timecapsule.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 偏好设置管理器
 * 存储用户首次启动同意状态等轻量数据
 */
class PreferenceManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /** 用户是否已同意免责声明 */
    var hasAgreedDisclaimer: Boolean
        get() = prefs.getBoolean(KEY_AGREED, false)
        set(value) = prefs.edit().putBoolean(KEY_AGREED, value).apply()

    companion object {
        private const val PREFS_NAME = "timecapsule_prefs"
        private const val KEY_AGREED = "has_agreed_disclaimer"

        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}