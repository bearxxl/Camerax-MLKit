package com.dgty.vision

import RopeAlgorithms
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.dgty.vision.base.SupportFragment
import com.dgty.vision.databinding.ActivityVisionBinding
import com.dgty.vision.databinding.FragmentVisionBinding
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Title: VisionActivity.kt
 * Description: 相机预览页面
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-03 11:22
 * Created by xxl.
 */
class VisionFragment : SupportFragment<FragmentVisionBinding>() {
    private lateinit var viewBinding: FragmentVisionBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetector: PoseDetector
    private lateinit var ropeAlgorithms: RopeAlgorithms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = FragmentVisionBinding.inflate(layoutInflater)
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Request camera permissions

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }


    }

    private fun startCamera() {
        var cameraController = LifecycleCameraController(this@VisionFragment.mContext)
        val previewView: PreviewView = viewBinding.viewFinder

        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options);
        ropeAlgorithms = RopeAlgorithms()
        ropeAlgorithms.count.observe(this, Observer {
            viewBinding.tvCount.text = it.toString() + "个"
        })
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(mContext),
            MlKitAnalyzer(
                listOf(poseDetector),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(mContext)
            ) { result: MlKitAnalyzer.Result? ->
                var results = result?.getValue(poseDetector)

                if (results != null) {
                    LogUtils.dTag("xxl", GsonUtils.toJson(results))
                    ropeAlgorithms.calculate(results)

                }
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    /*  fun decode(imageProxy: ImageProxy) {
          var image = imageProxy.image
          LogUtils.dTag("xxl", GsonUtils.toJson(image))
          var rotationDegrees = imageProxy.imageInfo.rotationDegrees
          if (image != null) {
              var mediaImage =
                  InputImage.fromMediaImage(image, rotationDegrees)

              poseDetector.process(mediaImage).addOnSuccessListener {
                  LogUtils.dTag("xxl", GsonUtils.toJson(it))

              }.addOnFailureListener {
                  ToastUtils.showLong(it.message)
              }
          }

      }*/

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            mContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        if (poseDetector != null) {
            poseDetector?.close()
        }

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
                showInfo("Permissions not granted by the user.")
            }
        }
    }
}