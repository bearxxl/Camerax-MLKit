
/**
 * Title: DurationFetchable
 * Description:计算给定的时间值是否在样本数据中不连续。
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-01 15:23
 * Created by xxl.
 */
abstract class DurationFetchable {
    /// - Parameter duration: 一个时间段。
    /// - Returns: 如果给定的时间值不连续，返回 true。
    abstract fun discontinuous(_duration: Long): Boolean

}