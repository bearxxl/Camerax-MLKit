package com.example.camerax_mlkit

import android.os.Bundle
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.core.content.ContextCompat
import com.dgty.vision.base.BaseActivity
import com.example.camerax_mlkit.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        /*val imageAnalysis = ImageAnalysis.Builder()
            // 宽高比
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            // 分辨率
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ImageAnalysis.Analyzer { imageProxy ->


        })*/
    }

    override fun initData() {
        TODO("Not yet implemented")
    }

}