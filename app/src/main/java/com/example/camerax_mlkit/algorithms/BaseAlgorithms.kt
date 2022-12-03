package com.example.camerax_mlkit.algorithms

import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.atan2

/**
 * Title: BaseAlgorithms
 * Description: 基础算法封装
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-11-30 17:26
 * Created by xxl.
 */
abstract class BaseAlgorithms {
    //次数
    var count : MutableLiveData<Integer> = MutableLiveData()

    //计算
    abstract fun calculate(pose: Pose)
    fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            (atan2(
                lastPoint.getPosition().y - midPoint.getPosition().y,
                lastPoint.getPosition().x - midPoint.getPosition().x
            )
                    - atan2(
                firstPoint.getPosition().y - midPoint.getPosition().y,
                firstPoint.getPosition().x - midPoint.getPosition().x
            )).toDouble()
        )
        result = Math.abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    fun yDis(from: PoseLandmark, to: PoseLandmark): Float {
        return to.position3D.y - from.position3D.y
    }


}