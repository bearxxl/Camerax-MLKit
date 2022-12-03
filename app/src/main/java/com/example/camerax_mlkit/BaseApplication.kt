package com.example.camerax_mlkit

import android.app.Application
import com.blankj.utilcode.util.Utils

/**
 * Title: BaseApplication
 * Description:
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-11-30 16:28
 * Created by xxl.
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}