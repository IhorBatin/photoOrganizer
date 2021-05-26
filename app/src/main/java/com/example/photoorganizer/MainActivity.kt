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
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.adapters.CustomImageAdapter
import com.example.photoorganizer.databinding.ActivityMainBinding
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.utils.FileUtil
import com.example.photoorganizer.utils.REQUEST_IMAGE_CAPTURE
import timber.log.Timber
import java.io.File

// Guide-> https://developer.android.com/training/camera/photobasics
// https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media

// TODO: Add RecyclerView to display all pics.
// TODO: Figure out why/what files are being generated when pic is not taken...

class MainActivity : AppCompatActivity() {
    lateinit var bundledMainActivity: ActivityMainBinding
    lateinit var fileUtil: FileUtil
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        fileUtil = FileUtil(this, applicationContext)
        recyclerView = bundledMainActivity.rvImagesRecycler

        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            fileUtil.dispatchTakePictureIntent()
        }
    }

    override fun onStart() {
        super.onStart()

        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val isDir = storageDir?.isDirectory.toString()

        Timber.tag(DEBUG_TAG).d("File: ${storageDir?.absolutePath} is dir=$isDir ")
        val files =  storageDir?.listFiles()
        Timber.tag(DEBUG_TAG).d("${files?.size ?: -1}")

        val rvAdapter = CustomImageAdapter(files as Array<File>)
        recyclerView.adapter = rvAdapter


        files?.forEach { file ->
            Timber.tag(DEBUG_TAG).d("File: ${file.name} isFile= ${file.isFile}")
            Timber.tag(DEBUG_TAG).d("File: ${file.name} parent= ${file.parentFile}")
            //file.delete()

            //fileUtil.setImageFromPath(file.absolutePath, bundledMainActivity.ivTestImage)
        }
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