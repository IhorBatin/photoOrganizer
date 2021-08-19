package com.example.photoorganizer.view

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
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

// TODO: Add fullscreen image view
// TODO: Add ability to share multiple images
// TODO: Add ability to delete image / multiple images
// TODO: Add layer ability to add folders inside folders

/** Fixes and Bugs */
// TODO: Fix directory being able to share same as regular image file

/** Future Plans/Features */
// TODO: Add locking app and specific folder feature [use ext functions on File obj to to do this]
// TODO: Add ability to change num of columns (1-2-3-4-5 on pinch or zoom with fingers/ add chooser on top)
// TODO: When Navigated to folder update its name in toolbar instead of app name
// TODO: apply default picture for folder. OR add ability to choose folder color.

class MainActivity : AppCompatActivity() {
    private lateinit var bundledMainActivity: ActivityMainBinding
    private val imagesViewModel: ImagesViewModel by viewModels()
    private lateinit var fileUtil: FileUtil
    private lateinit var recyclerView: RecyclerView
    private lateinit var customImageAdapter: CustomImageAdapter

    private lateinit var rootDir: File
    private var spanCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        fileUtil = FileUtil(this, applicationContext)

        rootDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        imagesViewModel.getFilesByDate(rootDir)
        //Timber.tag(DEBUG_TAG).d("Root = ${rootDir.path}")

        setupObservers()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        imagesViewModel.updateFiles(rootDir)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.top_munu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.miTakePhoto -> {
                handleTakePhotoClick()
                true
            }
            R.id.miImportPhoto -> {
                handleImportPhotoClick()
                true
            }
            R.id.miCreateFolder -> {
                handleCreateNewFolderClick()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun handleTakePhotoClick() {
        fileUtil.dispatchTakePictureIntent(rootDir)
    }

    private fun handleImportPhotoClick() {
        fileUtil.dispatchImportImagesIntent()
    }

    private fun handleCreateNewFolderClick() {
        //val newFile = fileUtil.createNewDirectory("testing5")
        fileUtil.showNewFolderAlert(imagesViewModel, rootDir)
        //Timber.tag(DEBUG_TAG).d("Created: ${newFile.absolutePath}")
        //imagesViewModel.updateFiles(root)
    }

    override fun onStart() {
        super.onStart()
        //imagesViewModel.getFilesByDate(root)

        val files =  imagesViewModel.imageListLiveData.value
        files?.forEach { file ->
            //Timber.tag(DEBUG_TAG).d("File: ${file.name} isFile= ${file.isFile}")
            //Timber.tag(DEBUG_TAG).d(" \\_canRead= ${file.canRead()} | freeSpace= ${file.freeSpace}")
            //file.delete()

            //fileUtil.setImageFromPath(file.absolutePath, bundledMainActivity.ivTestImage)
        }
    }

    override fun onBackPressed() {
        if (rootDir.name == Environment.DIRECTORY_PICTURES) {
            super.onBackPressed()
        }
        else {
            rootDir = rootDir.parentFile
            imagesViewModel.updateFiles(rootDir)
        }
    }

    private fun setupObservers() {
        imagesViewModel.imageListLiveData.observe(this , Observer { imagesList ->
            Timber.tag(DEBUG_TAG).d("Total Files: ${imagesList.size}")
            customImageAdapter.notifyDataSetChanged()
        })
    }

    private fun setupRecyclerView() {
        // Setting RV
        recyclerView = rvImagesRecycler.apply {
            customImageAdapter = CustomImageAdapter(imagesViewModel)
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = customImageAdapter
            itemAnimator = DefaultItemAnimator()
        }

        // Setting RV ClickListeners
        setupRVListeners()
    }

    private fun setupRVListeners() {
        customImageAdapter.onImageClick = { file ->
            Timber.tag(DEBUG_TAG).d("Clicked: '${file.name}'")
            if (file.isDirectory) handleOnDirectoryClick(file)
            else handleOnImageClick(file)

            imagesViewModel.updateFiles(rootDir)
        }

        customImageAdapter.onImageLongClick = { file ->
            Timber.tag(DEBUG_TAG).d("Long Clicked: '${file.name}'")
            //file.delete()

            //imagesViewModel.getFilesByDate(rootDir)
            //customImageAdapter.notifyDataSetChanged()

            /** Works but creates temp file in general directory */
            /*val b = BitmapFactory.decodeFile(image.path)
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            val bytes = ByteArrayOutputStream()
            b.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(contentResolver, b, "Title---", null)
            val imageUri: Uri = Uri.parse(path)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.putExtra(Intent.EXTRA_STREAM, imageUri)
            startActivity(Intent.createChooser(share, "Select"))*/

            /** Works but weird way of sharing */
            val uri = getUriForFile(this, packageName, file)
            val intent = ShareCompat.IntentBuilder.from(this)
                .setStream(uri) // uri from FileProvider
                .setType("image/jpeg")
                .intent
                .setAction(ACTION_SEND) //Change if needed
                .setDataAndType(uri, "image/*")
                .addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }
    }

    private fun handleOnImageClick(image: File) {

    }

    private fun handleOnDirectoryClick(dir: File) {
        rootDir = dir
        imagesViewModel.getFilesByDate(rootDir)
        Timber.tag(DEBUG_TAG).d("New root = ${rootDir.path}")
        Timber.tag(DEBUG_TAG).d(" Contains files = ${rootDir.listFiles().size}")
        //Timber.tag(DEBUG_TAG).d(" Contains files = ${rootDir}")
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
                    Timber.tag(DEBUG_TAG).d("Import #$i -> $currentImageUri")
                    val photoFile: File? = try {
                        fileUtil.createImageFile(rootDir)
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
        // Updating date in VM
        imagesViewModel.updateFiles(rootDir)
        //customImageAdapter.notifyDataSetChanged()
    }

}