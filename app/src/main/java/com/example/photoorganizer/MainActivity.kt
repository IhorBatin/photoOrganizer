package com.example.photoorganizer

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.example.photoorganizer.databinding.ActivityMainBinding
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.utils.FileUtil
import com.example.photoorganizer.utils.REQUEST_IMAGE_CAPTURE
import timber.log.Timber
import java.io.File

// TODO: Guide-> https://developer.android.com/training/camera/photobasics
// https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media

class MainActivity : AppCompatActivity() {
    lateinit var bundledMainActivity: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            val fileUtil: FileUtil = FileUtil(this, applicationContext)
            fileUtil.dispatchTakePictureIntent()
        }


    }

    override fun onStart() {
        super.onStart()

        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val absPath = storageDir?.absolutePath
        val isDir = storageDir?.isDirectory.toString()
        Timber.tag(DEBUG_TAG).d("File $absPath is dir=$isDir ")
    }

    /**
     * Returned image should be coming here if takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
     * is disabled in Util class
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.tag(DEBUG_TAG).d("onActivityResult()")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Timber.tag(DEBUG_TAG).d("image taken ...")
            //val imageBitmap = data?.extras?.get("data") as Bitmap
            //val image: ImageView = findViewById(R.id.ivTestImage)
            //image.setImageBitmap(imageBitmap)
        }
    }
}