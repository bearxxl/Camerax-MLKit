
import com.blankj.utilcode.util.LogUtils
import com.dgty.vision.bean.JumpType
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.lang.Math.abs

/**
 * Title: BaseAlgorithms
 * Description: 跳绳算法封装
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-11-30 17:26
 * Created by xxl.
 */
class RopeAlgorithms : BaseAlgorithms() {

    lateinit var pose: Pose

    /// A set of nose samples.
    var noseSamples = ArrayList<PoseLandmark>()

    /// A set of root samples.
    var leftHipSamples = ArrayList<PoseLandmark>()

    /// A set of left ankle samples.
    var leftAnkleSamples = ArrayList<PoseLandmark>()

    /// A set of right ankle samples.
    var rightAnkleSamples = ArrayList<PoseLandmark>()

    var lastState: Int = 0

    /// A Time taken for action.
    var lastTime = 0L
    var nowTime = 0L
    var sampleSize = 5 //样本数据
    var ropeCount = 0 //跳绳数据

    override fun calculate(pose: Pose) {
        this.pose = pose
        if (pose.allPoseLandmarks == null) return

        /* if (lastTime == 0L) {
             return
         }*/


        var nose = pose.getPoseLandmark(PoseLandmark.NOSE)
        var lefyEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
        var lefyEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
        var leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
        var rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
        var rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
        var rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
        var leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        var rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
        var leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
        var rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

        var leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        var rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        var leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        var rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        var leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        var rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        var leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        var rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        var leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        var rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        var leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        var rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        var leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        var rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        var leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        var rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        var leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        var rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        var leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        var rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        var leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        var rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

        if (nose != null) {
            noseSamples.add(nose)
        }
        if (noseSamples.size > sampleSize) {
            noseSamples.removeFirst()
        }
        if (leftHip != null) {
            leftHipSamples.add(leftHip)
        }
        if (leftHipSamples.size > sampleSize) {
            leftHipSamples.removeFirst()
        }
        if (leftAnkle != null) {
            leftAnkleSamples.add(leftAnkle)
        }
        if (leftAnkleSamples.size > sampleSize) {
            leftAnkleSamples.removeFirst()
        }
        if (rightAnkle != null) {
            rightAnkleSamples.add(rightAnkle)
        }
        if (rightAnkleSamples.size > sampleSize) {
            rightAnkleSamples.removeLast()
        }

        nowTime = System.currentTimeMillis()
        /* actionDuration += nowTime - lastTime
         if (actionDuration > 0.5) {
             actionDuration = 0
             lastState = JumpType.UNKNOW
             return
         }*/
        if (noseSamples.size < sampleSize) {
            return
        }
        if (leftHipSamples.size < sampleSize) {
            return
        }
        if (leftAnkleSamples.size < sampleSize) {
            return
        }
        if (rightAnkleSamples.size < sampleSize) {
            return
        }
        //左肩点。
        if (rightElbow != null && rightShoulder != null && rightHip != null && leftElbow != null && leftShoulder != null && leftHip != null) {
            if (isArmsOpen(rightElbow, rightShoulder, rightHip) && isArmsOpen(leftElbow, leftShoulder, leftHip)) {
                //手臂打开
                var state = getCurrentState(leftShoulder, leftHip)
                if (state != JumpType.UNKNOW) {
                    if ((state == JumpType.DOWN && lastState == JumpType.UP) || (state == JumpType.UP && lastState == JumpType.DOWN)) {
                        ropeCount++
                        count.postValue(Integer(ropeCount / 2))
                        LogUtils.dTag("count", "ropeCount = " + ropeCount / 2)
                    }
                    lastState = state //上次状态
                }

            }
        }

        lastTime = System.currentTimeMillis()

    }

    fun isArmsOpen(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Boolean {
        var rightAngle: Double = getAngle(firstPoint, midPoint, lastPoint)
        var rightMatch = (20 <= rightAngle && rightAngle <= 50)
        return rightMatch
    }


    fun getCurrentState(leftShoulder: PoseLandmark?, leftHip: PoseLandmark?): Int {
        var distance = abs(getDistance(leftShoulder, leftHip))
        var state = JumpType.UNKNOW
        var s = "UNKNOW"
        var noseDis = noseSamples.get(sampleSize - 1).position3D.y - noseSamples.get(0).position3D.y
        var leftHipDis = leftHipSamples.get(sampleSize - 1).position3D.y - leftHipSamples.get(0).position3D.y
        var leftAnkleDis = leftAnkleSamples.get(sampleSize - 1).position3D.y - leftAnkleSamples.get(0).position3D.y
        var rightAnkleDis = rightAnkleSamples.get(sampleSize - 1).position3D.y - rightAnkleSamples.get(0).position3D.y
        LogUtils.dTag(
            "xxlDis", "noseDis = " + noseDis + "leftHipDis = " + leftHipDis + "leftAnkleDis = " + leftAnkleDis + "rightAnkleDis = "
                    + rightAnkleDis
        )
        LogUtils.d("xxlavg", "0.025 dis =  " + (-0.025 * distance) + "0.004 dis =  " + (0.004 * distance) + "0.003 dis =  " + (0.003 * distance))
        if ((noseDis < -0.025 * distance) && (leftHipDis < -0.025 * distance)/* && (leftAnkleDis < -0.025 * distance) && (rightAnkleDis < -0.025 * distance)*/) {
            state = JumpType.DOWN
            s = "DOWN"
        }
        if ((noseDis > 0.04 * distance) && (leftHipDis > 0.04 * distance) /*&& (leftAnkleDis > 0.03 * distance) && (rightAnkleDis > 0.03 * distance)*/) {
            state = JumpType.UP
            s = "UP"
        }

        LogUtils.dTag("xxlstate", s)
        return state
    }

    private fun getDistance(endLShoulder: PoseLandmark?, startLShoulder: PoseLandmark?): Float {
        return if (endLShoulder != null && startLShoulder != null && endLShoulder.position3D != null && startLShoulder.position3D != null) {
            endLShoulder.position3D.y - startLShoulder.position3D.y
        } else {
            return 0f
        }
    }

}