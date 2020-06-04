package com.greimul.finddots

import android.graphics.Canvas
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
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
    lateinit var displayMetrics:DisplayMetrics
    lateinit var imgView:ImageView

    var isMaskSet:Boolean = false
    var cameraWidth:Int = 0
    var cameraHeight:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        displayMetrics = resources.displayMetrics
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
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        originMat = inputFrame.rgba()

        val rectX = originMat.width()/8+(originMat.width()/4*3-originMat.height()/4*3)/2
        val rectY = originMat.height()/8
        if(!isMaskSet) {

            matRoi = Rect(rectX,rectY,originMat.height()/4*3,originMat.height()/4*3)

            maskMat = Mat.zeros(originMat.size(), originMat.type())
            maskMat.submat(matRoi).setTo(Scalar.all(255.0))
            isMaskSet = true
        }

        var res = Mat()
        Core.bitwise_and(originMat,maskMat,res)
        //Imgproc.cvtColor(res,res,Imgproc.COLOR_RGBA2GRAY)
        var circle = findDots(res.nativeObjAddr)
        Imgproc.rectangle(originMat,Point(rectX.toDouble(),rectY.toDouble()),Point(rectX+originMat.height()/4*3.0,rectY+originMat.height()/4*3.0),Scalar(255.0,0.0,0.0),5)
        Imgproc.circle(originMat,Point(circle[0],circle[1]),circle[2].toInt(),Scalar(255.0,0.0,0.0),5)
        return originMat
    }
    override fun onCameraViewStarted(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height

        Log.d("pos","${cameraWidth} ${cameraHeight}")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run{Log.d("touchPos","${event.x} ${event.y}")}
        return super.onTouchEvent(event)
    }
    override fun onCameraViewStopped() {}

    private external fun findDots(input:Long):DoubleArray
}