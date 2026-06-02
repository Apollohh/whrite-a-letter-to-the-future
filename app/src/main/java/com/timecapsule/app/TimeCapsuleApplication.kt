package com.timecapsule.app

import android.app.Application

/**
 * 应用入口
 * 全局初始化
 */
class TimeCapsuleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 无需额外初始化，Room和Storage都是懒加载
    }
}