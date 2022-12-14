package com.dgty.vision

import RopeAlgorithms
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SnackbarUtils
import com.dgty.vision.base.SupportActivity
import com.dgty.vision.databinding.ActivityVisionBinding
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class VisionActivity : SupportActivity<ActivityVisionBinding>() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetector: PoseDetector
    private lateinit var ropeAlgorithms: RopeAlgorithms
    lateinit var snackbarUtils: SnackbarUtils


    //摄像头
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    override fun initView(savedInstanceState: Bundle?) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Request camera permissions

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun initData() {
    }

    private fun startCamera() {
        var cameraController = LifecycleCameraController(baseContext)
        var previewView: PreviewView = mBinding.viewFinder
        mBinding.llBottom.visibility = View.VISIBLE
        showWarn(true)
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options);
        ropeAlgorithms = RopeAlgorithms()
        ropeAlgorithms.count.observe(this, Observer {
            mBinding.tvCount.text = it.toString() + "个"
        })
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(poseDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                var results = result?.getValue(poseDetector)

                if (results != null) {
                    LogUtils.dTag("xxl", GsonUtils.toJson(results))
                    if (checkAllPose(results.allPoseLandmarks)) {
                        showWarn(false)
                    } else {
                        showWarn(true)
                    }
                    ropeAlgorithms.calculate(results)

                } else {
                    showWarn(true)
                }
            }
        )
        //预览尺寸
        var outPutSize = CameraController.OutputSize(Size(1920, 1080))
        cameraController.previewTargetSize = outPutSize

        LogUtils.dTag("xxlsize", outPutSize.toString())

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }


    // 是否全身骨骼点都在
    private fun checkAllPose(poseList: List<PoseLandmark>): Boolean {
        var result = false
        if (poseList != null && poseList.size >= 33) {
            result = true
            for (mark in poseList) {
                if (null == mark || mark.position3D == null) {
                    result = false
                }
            }
        }
        LogUtils.dTag("xxl", "checkAllPose = " + result)
        return result
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showWarn(boolean: Boolean) {
        LogUtils.dTag("xxl", "showWarn = " + boolean)
        if (boolean) {
            mBinding.llBottom.visibility = View.VISIBLE
        } else {
            mBinding.llBottom.visibility = View.GONE
        }

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        poseDetector.close()
    }

    companion object {
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


}