package com.example.photoorganizer.view

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.adapters.CustomImageAdapter
import com.example.photoorganizer.databinding.ActivityMainBinding
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

// RecyclerOnClick -> https://stackoverflow.com/questions/24471109/recyclerview-onclick

// TODO: Add way of creating new folders by user
// TODO: Add fullscreen image view
// TODO: Add ability to share image
// TODO: Add ability to delete image
// TODO: Implement logic do distinguish images from folders and apply default picture for folder.

/** Future plans/Features */
// TODO: Add locking app and specific folder feature
// TODO: Add ability to change num of columns (1-2-3-4-5 on pinch or zoom with fingers)

class MainActivity : AppCompatActivity() {
    private lateinit var bundledMainActivity: ActivityMainBinding
    val imagesViewModel: ImagesViewModel by viewModels()
    private lateinit var fileUtil: FileUtil
    private lateinit var recyclerView: RecyclerView
    private lateinit var customImageAdapter: CustomImageAdapter

    private var spanCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        fileUtil = FileUtil(this, applicationContext)
        recyclerView = rvImagesRecycler
        //recyclerView.nes

        setupObservers()
        val root: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imagesViewModel.getFilesByDate(root)

        //customImageAdapter = CustomImageAdapter(imagesViewModel.imageListLiveData.value as Array<File>, this)
        rvImagesRecycler.apply {
            customImageAdapter = CustomImageAdapter(imagesViewModel.imageListLiveData.value as Array<File>)
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = customImageAdapter
            itemAnimator = DefaultItemAnimator()
        }

        //customImageAdapter.setOnImageClickListener(CustomImageAdapter.OnImageClickedListener) {})

        //rvImagesRecycler.addOnItemTouchListener(RecyclerView.OnItemTouchListener() )

        btnTest.setOnClickListener {
            fileUtil.dispatchTakePictureIntent()
        }

        btnImport.setOnClickListener{
            fileUtil.dispatchImportImagesIntent()
        }
    }



    override fun onStart() {
        super.onStart()

        //val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //val isDir = storageDir?.isDirectory.toString()
        //ImagesRepository.fetchAllFilesFromDir(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES))

        //Timber.tag(DEBUG_TAG).d("File: ${storageDir?.absolutePath} is dir=$isDir ")

        val files =  imagesViewModel.imageListLiveData.value
        //Timber.tag(DEBUG_TAG).d("Total Files: ${files?.size}")


        files?.forEach { file ->
            //Timber.tag(DEBUG_TAG).d("File: ${file.name} isFile= ${file.isFile}")
            //Timber.tag(DEBUG_TAG).d(" \\_canRead= ${file.canRead()} | freeSpace= ${file.freeSpace}")
            //file.delete()

            //fileUtil.setImageFromPath(file.absolutePath, bundledMainActivity.ivTestImage)
        }
    }

    private fun setupObservers() {
        imagesViewModel.imageListLiveData.observe(this , Observer { imagesList ->
            Timber.tag(DEBUG_TAG).d("Total Files: ${imagesList.size}")
            customImageAdapter.updateImagesList(imagesList  as Array<File>)
            customImageAdapter.notifyItemChanged(3)
        })
    }

    private fun checkForGestures() {
        /*bundledMainActivity.root.setOnLongClickListener {

        }*/
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