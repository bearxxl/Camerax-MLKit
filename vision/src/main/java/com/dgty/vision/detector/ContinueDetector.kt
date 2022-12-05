import com.dgty.vision.bean.State

/**
 * Title: ContinueDetector
 * Description: 不连续检测器
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-01 15:22
 * Created by xxl.
 */
class ContinueDetector {
    private var lastTime: Long = 0

    private var actionDuration: Long = 0

    private var state: String = State.idle

    //不连续次数
    private var suspendCount = 0
    private var isSuspended = false

    fun isSuspended() {
        if (isSuspended) {
            suspendCount++
        }
    }

    lateinit var fetcher: DurationFetchable
    fun tick() {
        if (state.equals(State.running)) {
            var deltaTime = System.currentTimeMillis() - lastTime
            actionDuration += deltaTime
            // The action takes `timeTnterval` seconds to complete. If the count is
            // not completed after this time, the action may timeout.
            if (fetcher != null && fetcher.discontinuous(actionDuration)) {
                isSuspended = true
                state = State.idle
            }
        }
        lastTime = System.currentTimeMillis()
    }

    fun markingBegin() {
        state = State.running
        lastTime = System.currentTimeMillis()
    }

    fun markingEnd() {
        actionDuration = 0
        isSuspended = false
        state = State.idle
        lastTime = 0
    }
}
