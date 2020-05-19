package com.greimul.finddots

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CameraActivity : AppCompatActivity(),CameraBridgeViewBase.CvCameraViewListener2 {

    lateinit var mainCamera:CameraBridgeViewBase
    lateinit var maskMat:Mat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        maskMat = Mat(960,1280,0)

        var targetX = Point(maskMat.rows()/3.0,maskMat.cols()/3.0*2.0)
        var targetY = Point(maskMat.rows()/3*2.0,maskMat.cols()/3.0)

        //var targetX = Pair(maskMat.rows()/3,maskMat.cols()/3)
        //var targetY = Pair(maskMat.rows()/3*2,maskMat.cols()/3*2)
/*
        for(i in targetX.first..targetY.first)
            for(j in targetX.second..targetY.second)
                maskMat.get(i,j)[0] = 255.0
*/
        Imgproc.rectangle(maskMat,targetX,targetY,Scalar(255.0),10,Imgproc.FILLED)

        mainCamera = camera_main
        mainCamera.setCameraPermissionGranted()
        mainCamera.enableView()
        mainCamera.setCvCameraViewListener(this)
        mainCamera.setCameraIndex(0)
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        var res = Mat()
        Core.bitwise_and(inputFrame.gray(),maskMat,res)
        return res
    }
    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}
}