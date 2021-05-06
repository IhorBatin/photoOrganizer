package com.example.photoorganizer

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.photoorganizer.databinding.ActivityMainBinding
import com.example.photoorganizer.utils.FileUtil
import java.io.File

// TODO: Guide-> https://developer.android.com/training/camera/photobasics

class MainActivity : AppCompatActivity() {
    lateinit var bundledMainActivity: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            val fileUtil: FileUtil = FileUtil(this, applicationContext)
            fileUtil.dispatchTakePictureIntent()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val image: ImageView = findViewById(R.id.ivTestImage)
            image.setImageBitmap(imageBitmap)
        }
    }
}