package com.dgty.vision.bean

/**
 * Title: Movement
 * Description: 用来表示一个动作
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-05 14:07
 * Created by xxl.
 */
class Movement {
    var begin: Long = 0

    var end: Long = 0

    fun duration(): Long {
        return end - begin
    }


    fun isFinished(): Boolean {
        return end > begin
    }

    fun init() {
        begin = System.currentTimeMillis()
    }

    fun finish() {
        end = System.currentTimeMillis()
    }
}