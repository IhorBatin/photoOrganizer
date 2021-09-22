package com.example.photoorganizer.view

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.CustomImageAdapter
import com.example.photoorganizer.databinding.ActivityMainBinding
import com.example.photoorganizer.ext.*
import com.example.photoorganizer.utils.*
import com.example.photoorganizer.viewmodel.ImagesViewModel
import com.example.photoorganizer.viewmodel.ViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var bundledMainActivity: ActivityMainBinding
    private lateinit var imagesViewModel: ImagesViewModel
    private lateinit var fileUtil: FileUtil
    private lateinit var recyclerView: RecyclerView
    private lateinit var customImageAdapter: CustomImageAdapter

    private lateinit var rootDir: File
    private var spanCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)
        supportActionBar?.title = getString(R.string.home_text)

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        fileUtil = FileUtil(this, applicationContext)

        rootDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        imagesViewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(ImagesViewModel::class.java)
        imagesViewModel.setRootDir(rootDir)
        //Timber.tag(DEBUG_TAG).d("Root = ${rootDir.path}")

        setupObservers()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        //imagesViewModel.updateFiles(rootDir)
        imagesViewModel.setRootDir(imagesViewModel.getCurrentRoot())
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
        fileUtil.dispatchTakePictureIntent(imagesViewModel.getCurrentRoot())
    }

    private fun handleImportPhotoClick() {
        fileUtil.dispatchImportImagesIntent()
    }

    private fun handleCreateNewFolderClick() {
        //val newFile = fileUtil.createNewDirectory("testing5")
        fileUtil.showNewFolderAlert(imagesViewModel, imagesViewModel.getCurrentRoot())
        //Timber.tag(DEBUG_TAG).d("Created: ${newFile.absolutePath}")
        //imagesViewModel.updateFiles(root)
    }

    override fun onStart() {
        super.onStart()
        //imagesViewModel.getFilesByDate(root)

        val files =  imagesViewModel.filesListLiveData.value
        files?.forEach { file ->
            //Timber.tag(DEBUG_TAG).d("File: ${file.name} isFile= ${file.isFile}")
            //Timber.tag(DEBUG_TAG).d(" \\_canRead= ${file.canRead()} | freeSpace= ${file.freeSpace}")
            //file.delete()

            //fileUtil.setImageFromPath(file.absolutePath, bundledMainActivity.ivTestImage)
        }
    }

    override fun onBackPressed() {
        if (bundledMainActivity.clImageOptions.isShown ||
                bundledMainActivity.clDirectoryOptions.isShown) {
            toggleImageLongClickOptions(false)
            toggleDirectoryLongClickOptions(false)
            return
        }
        if (imagesViewModel.getCurrentRoot()?.name == Environment.DIRECTORY_PICTURES) {
            super.onBackPressed()
        }
        else {
            imagesViewModel.setRootDir(imagesViewModel.getCurrentRoot()?.parentFile)
        }
    }

    private fun setupObservers() {
        imagesViewModel.filesListLiveData.observe(this , { imagesList ->
            Timber.tag(DEBUG_TAG).d("Total Files: ${imagesList.size}")
            customImageAdapter.notifyDataSetChanged()
        })

        imagesViewModel.rootDirLiveData.observe(this, {
            supportActionBar?.title = it.name
        })
    }

    private fun setupRecyclerView() {
        // Setting RV
        recyclerView = bundledMainActivity.rvImagesRecycler.apply {
            customImageAdapter = CustomImageAdapter(imagesViewModel)
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = customImageAdapter
            itemAnimator = DefaultItemAnimator()
        }

        // Setting RV ClickListeners
        setupRVListeners()
    }

    private fun setupRVListeners() {
        customImageAdapter.onImageClick = { position ->
            toggleImageLongClickOptions(false)
            toggleDirectoryLongClickOptions(false)
            val fileClicked: File = imagesViewModel.filesListLiveData.value!![position]

            Timber.tag(DEBUG_TAG).d("Clicked: '${fileClicked.name}'")
            if (fileClicked.isDirectory) handleOnDirectoryClick(fileClicked)
            else handleOnImageClick(position)
        }

        customImageAdapter.onImageLongClick = { position ->
            toggleImageLongClickOptions(false)
            toggleDirectoryLongClickOptions(false)
            val fileLongClicked: File = imagesViewModel.filesListLiveData.value!![position]
            Timber.tag(DEBUG_TAG).d("Long Clicked: '${fileLongClicked.name}'")

            if (fileLongClicked.isDirectory) {
                handleOnDirectoryLongClick(fileLongClicked, position)
            }
            else {
                handleOnImageLongClick(fileLongClicked, position)
            }
        }
    }

    private fun handleOnImageClick(clickPosition: Int) {
        startActivity(
            Intent(
                this,
                ScreenSlidePagerActivity::class.java
            ).putExtra(
                OPEN_AT_POS,
                imagesViewModel.gtePositionForImageSlider(clickPosition)
            )
        )
    }

    private fun handleOnImageLongClick(image: File, pos: Int) {
        toggleImageLongClickOptions(true)
        toggleDirectoryLongClickOptions(false)

        bundledMainActivity.ivDeleteImage.setOnClickListener {
            fileUtil.deleteImageFromImageAdapter(pos, imagesViewModel, customImageAdapter)
            toggleImageLongClickOptions(false)
        }

        bundledMainActivity.ivShareImage.setOnClickListener {
            fileUtil.dispatchShareImageIntent(image)
            toggleImageLongClickOptions(false)
        }
    }

    private fun handleOnDirectoryClick(dir: File) {
        toggleImageLongClickOptions(false)
        toggleDirectoryLongClickOptions(false)
        if (dir.isDirectoryPwdLocked(this)) fileUtil.showEnterPasswordAlert(imagesViewModel, dir)
        //if (dir.isDirectoryBiometricLocked(this)) fileUtil.showBiometricAuthenticator()
        else {
            imagesViewModel.setRootDir(dir)
            Timber.tag(DEBUG_TAG).d("New root = /${imagesViewModel.getCurrentRoot()?.name}")
            Timber.tag(DEBUG_TAG).d(" Contains files = ${imagesViewModel.getCurrentRoot()?.listFiles()?.size}")
        }
    }

    private fun handleOnDirectoryLongClick(dir: File, pos: Int) {
        toggleImageLongClickOptions(false)
        toggleDirectoryLongClickOptions(true)

        Timber.tag(DEBUG_TAG).d("Is ${dir.name} Locked: ${dir.isDirectoryLocked(this)}")
        Timber.tag(DEBUG_TAG).d("${dir.name} pass: ${dir.getDirectoryPassword(this)}")

        bundledMainActivity.ivDeleteDirectory.setOnClickListener {
            fileUtil.showDeleteDirectoryAlert(imagesViewModel, dir)
            toggleDirectoryLongClickOptions(false)
        }

        bundledMainActivity.ivChangeDirColor.setOnClickListener {
            fileUtil.showChangeDirectoryColorAlert(imagesViewModel, dir)
            toggleDirectoryLongClickOptions(false)
        }

        bundledMainActivity.ivLockDirectory.setOnClickListener {
            if (dir.isDirectoryPwdLocked(this)) fileUtil.showDeletePasswordAlert(imagesViewModel, dir)
            else fileUtil.showChooseLockTypeAlert(imagesViewModel, dir)
            toggleDirectoryLongClickOptions(false)
        }
    }

    private fun toggleImageLongClickOptions(showOptions: Boolean) {
        bundledMainActivity.clImageOptions.visibility = if (showOptions) View.VISIBLE else View.GONE
    }

    private fun toggleDirectoryLongClickOptions(showOptions: Boolean) {
        bundledMainActivity.clDirectoryOptions.visibility = if (showOptions) View.VISIBLE else View.GONE
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
                        fileUtil.createImageFile(imagesViewModel.getCurrentRoot())
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