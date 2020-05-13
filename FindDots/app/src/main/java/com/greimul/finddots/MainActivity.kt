package com.greimul.finddots

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                imageView.setImageBitmap(getBitmapFromUri(uri))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = imageview_main
        button_load.setOnClickListener {
            performFileSearch()
        }
        button_count.setOnClickListener {
            var inputMat = Mat()
            Utils.bitmapToMat(imageView.drawable.toBitmap(),inputMat)
            var cnt = findDots(inputMat.nativeObjAddr)
            textview_count.text = "Count: $cnt"
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

    external fun findDots(input:Long):Int

    companion object {
        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("opencv_java4")
        }
    }
}
