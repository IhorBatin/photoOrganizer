package com.example.photoorganizer.view

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.CustomImageAdapter
import com.example.photoorganizer.databinding.ActivityMainBinding
import com.example.photoorganizer.repository.ImagesRepository
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.utils.FileUtil
import com.example.photoorganizer.utils.REQUEST_IMAGE_CAPTURE
import com.example.photoorganizer.utils.REQUEST_IMAGE_IMPORT
import com.example.photoorganizer.viewmodel.ImagesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File
import java.io.IOException

// Guide-> https://developer.android.com/training/camera/photobasics
// https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media

// TODO: Add way of creating new folders by user
// TODO: Add fullscreen image view
// TODO: Add ability to share image
// TODO: Add ability to delete image
// TODO: Implement logic do distinguish images from folders and apply default picture for folder.

/** Future plans/Features */
// TODO: Add locking app and specific folder feature

class MainActivity : AppCompatActivity() {
    lateinit var bundledMainActivity: ActivityMainBinding
    private val imagesViewModel: ImagesViewModel by viewModels()
    private lateinit var fileUtil: FileUtil
    lateinit var recyclerView: RecyclerView
    lateinit var customImageAdapter: CustomImageAdapter

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

        btnImport.setOnClickListener{
            fileUtil.dispatchImportImagesIntent()
        }
    }

    override fun onStart() {
        super.onStart()

        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val isDir = storageDir?.isDirectory.toString()

        fileUtil.fetchAllFilesFromDir(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        Timber.tag(DEBUG_TAG).d("File: ${storageDir?.absolutePath} is dir=$isDir ")

        val files =  fileUtil.sortFilesByDate(storageDir?.listFiles())
        Timber.tag(DEBUG_TAG).d("Total Files: ${files?.size}")

        bundledMainActivity.rvImagesRecycler.apply {
            customImageAdapter = CustomImageAdapter(files as Array<File>)
            layoutManager = GridLayoutManager(context, 3)
            adapter = customImageAdapter
            itemAnimator = DefaultItemAnimator()
        }




        files?.forEach { file ->
            //Timber.tag(DEBUG_TAG).d("File: ${file.name} isFile= ${file.isFile}")
            //Timber.tag(DEBUG_TAG).d(" \\_canRead= ${file.canRead()} | freeSpace= ${file.freeSpace}")
            //file.delete()

            //fileUtil.setImageFromPath(file.absolutePath, bundledMainActivity.ivTestImage)
        }
    }

    /**
     * Returned image should be coming here if takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
     * is disabled in Util class
     */
    @SuppressWarnings("deprecation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //super.onActivityResult(requestCode, resultCode, data)
        Timber.tag(DEBUG_TAG).d("onActivityResult()")

        /** Handling return from image capture activity */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            // When image is not taken we simply delete empty file that was created for it
            //Timber.tag(DEBUG_TAG).d("image was NOT taken ... deleting empty file")
            File(fileUtil.currentPhotoPath).delete()
        }
        /** Handling return from image import activity */
        if (requestCode == REQUEST_IMAGE_IMPORT && resultCode == RESULT_OK) {
            Timber.tag(DEBUG_TAG).d("Image import selected ....")
            if (data != null && data.clipData != null) {
                val numOfImports = data.clipData?.itemCount ?: 0
                Timber.tag(DEBUG_TAG).d("Imported $numOfImports images")

                for (i in 0 until numOfImports) {
                    val currentImageUri = data.clipData?.getItemAt(i)?.uri
                    Timber.tag(DEBUG_TAG).e("Import #$i -> $currentImageUri")
                    val photoFile: File? = try {
                        fileUtil.createImageFile()
                    } catch (ex: IOException) {
                        Timber.tag(DEBUG_TAG).d("Error while creating file...")
                        null
                    }
                    val byteArray = currentImageUri?.let {
                        fileUtil.readBytesFromUri(it)
                    }
                    byteArray?.let { photoFile?.writeBytes(it) }
                }
            }
        }
    }



}