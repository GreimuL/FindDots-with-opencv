package com.greimul.finddots

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.FileDescriptor
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 10

    lateinit var imageView:ImageView
    lateinit var canvas:Canvas
    lateinit var bitmap: Bitmap


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val loadBitmap = getBitmapFromUri(uri)
                imageView.setImageBitmap(loadBitmap)
                bitmap = Bitmap.createBitmap(loadBitmap.width,loadBitmap.height,Bitmap.Config.ARGB_8888)
                canvas = Canvas(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this,CameraActivity::class.java))


        var drawBrush = Paint().apply {
            color = Color.parseColor("#000000")
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
            isDither = true
        }

        imageView = imageview_main
        button_load.setOnClickListener {
            performFileSearch()
        }
        button_count.setOnClickListener {
            var inputMat = Mat()
            Utils.bitmapToMat(imageView.drawable.toBitmap(),inputMat)
            var circle = findDots(inputMat.nativeObjAddr)
            textview_count.text = "Circle info: ${circle[0]} ${circle[1]} ${circle[2]}"

            canvas.drawARGB(0,0,0,0)
            canvas.drawCircle(circle[0].toFloat(),circle[1].toFloat(),circle[2].toFloat(),drawBrush)

            imageView.foreground = BitmapDrawable(resources,bitmap)
        }
    }
    fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                ?:return Bitmap.createBitmap(0,0,Bitmap.Config.ARGB_8888)
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private external fun findDots(input:Long):DoubleArray

    companion object {
        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("opencv_java4")
        }
    }
}
