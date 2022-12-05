package com.dgty.vision.utils

/**
 * Title: MathUtils
 * Description: 数学计算
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-05 14:12
 * Created by xxl.
 */
class MathUtils {
    /**
     * RMS均方根：
     * 所有数值，先将每个数值平方，然后将平方后的所有数值求和，再除以总体大小得到均值，再将均值开平方得到均方根
     */
    fun meanRoot(`in`: DoubleArray): Double {
        var sum = 0.0
        for (v in `in`) {
            sum = Math.pow(v, 2.0) + sum // Math.pow 平方
        }
        return Math.sqrt(sum / `in`.size) // Math.sqrt 开更
    }

    /**
     * 均值/算术平均数
     * 数组中所有数相加，总和除以数组长度
     */
    fun mean(`in`: DoubleArray): Double {
        var sum = 0.0
        for (v in `in`) {
            sum = sum + v
        }
        return sum / `in`.size
    }

    /**
     * 标准差
     * 先求数组的均值，然后将数组中的每个值先减去均值，在开平方，然后累加得到总和，
     * 然后将总和在除以数组长度得到均值，最后将将均值开根
     */
    fun standardDeviation(`in`: DoubleArray): Double {
        val t_mean = mean(`in`)
        var t_sumPerPow = 0.0
        for (i in `in`.indices) {
            t_sumPerPow = Math.pow(`in`[i] - t_mean, 2.0) + t_sumPerPow // Math.pow 平方
        }
        return Math.sqrt(t_sumPerPow / (`in`.size - 1))
    }

    /**
     * 计算偏度值
     * 先求数组均值， 然后将数组中的每个值先减去均值，在开3次方，然后累加得到总和，
     * 然后将总和在除以数组长度
     */
    fun skewness(`in`: DoubleArray): Double {
        val t_mean = mean(`in`) // 得到均值
        var s_mean = 0.0
        for (v in `in`) {       // 每个数减去均值后的3次方。统计和。
            s_mean = Math.pow(v - t_mean, 3.0) + s_mean
        }
        return s_mean / `in`.size // 返回 s_mean 的均值
    }

    /**
     * 6
     * 计算峰度值
     * 先求数组均值， 然后将数组中的每个值先减去均值，在开4次方，然后累加得到总和，
     * 然后将总和在除以数组长度得平均值，然后平均值减去3
     */
    fun kurtosis(`in`: DoubleArray): Double {
        val t_mean = mean(`in`) // 得到均值
        var s_mean = 0.0
        for (v in `in`) {       // 每个数减去均值后的4次方。统计和。
            s_mean = Math.pow(v - t_mean, 4.0) + s_mean
        }
        return s_mean / `in`.size - 3 // 返回 s_mean 的均值减去 3
    }

    /**
     * 计算过零率
     * 过零率：所有数字，两两相乘后的值是否小于0，小于则累加，即得到过零率值
     */
    fun zcr(`in`: DoubleArray): Double {
        var zcr = 0.0
        for (i in 1 until `in`.size) {
            if (`in`[i - 1] * `in`[i] < 0) {
                zcr++
            }
        }
        return zcr
    }

    /**
     * 斜方差（为无偏，即分母为n-1，而不是n）
     * 要求：两数组长度相等
     * 分别求x和y两个数组的均值mx和my,然后将两个数组对应下标的值各自减去各自数组的均值后在相乘，
     * 在累加，最后除以减一后的数组长度
     */
    fun cov(x: DoubleArray, y: DoubleArray): Double {
        val x_mean = mean(x)
        val y_mean = mean(y)
        var sum = 0.0
        for (i in x.indices) {
            sum = (x[i] - x_mean) * (y[i] - y_mean) + sum
        }
        return sum / (x.size - 1)
    }


}