package com.greimul.finddots

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CameraActivity : AppCompatActivity(),CameraBridgeViewBase.CvCameraViewListener2 {

    lateinit var mainCamera:CameraBridgeViewBase
    lateinit var maskMat:Mat
    lateinit var matRoi:Rect
    lateinit var originMat:Mat

    var isMaskSet:Boolean = false
    var cameraWidth:Int = 0
    var cameraHeight:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        //var targetX = Pair(maskMat.rows()/3,maskMat.cols()/3)
        //var targetY = Pair(maskMat.rows()/3*2,maskMat.cols()/3*2)
/*
        for(i in targetX.first..targetY.first)
            for(j in targetX.second..targetY.second)
                maskMat.get(i,j)[0] = 255.0
*/
        //Imgproc.rectangle(maskMat,targetX,targetY,Scalar(255.0),10,Imgproc.FILLED)

        mainCamera = camera_main
        mainCamera.holder.addCallback(object:SurfaceHolder.Callback2{
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
            override fun surfaceCreated(holder: SurfaceHolder?) {}
            override fun surfaceDestroyed(holder: SurfaceHolder?) {}
            override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {}
        })
        mainCamera.setCameraPermissionGranted()
        mainCamera.enableView()
        mainCamera.setCvCameraViewListener(this)
        mainCamera.setCameraIndex(0)

        matRoi = Rect(32,32,448,448)

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        originMat = inputFrame.rgba()

        if(!isMaskSet) {
            maskMat = Mat.zeros(originMat.size(), originMat.type())
            maskMat.submat(matRoi).setTo(Scalar.all(255.0))
            isMaskSet = true
        }

        var res = Mat()
        Core.bitwise_and(originMat,maskMat,res)
        Imgproc.cvtColor(res,res,Imgproc.COLOR_RGBA2GRAY)

        return res
    }
    override fun onCameraViewStarted(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height

        Log.d("pos","${cameraWidth} ${cameraHeight}")
    }
    override fun onCameraViewStopped() {}
}