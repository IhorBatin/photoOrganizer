package com.example.photoorganizer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var biometricUtil: BiometricUtil
    private lateinit var recyclerView: RecyclerView
    private lateinit var customImageAdapter: CustomImageAdapter

    private lateinit var rootDir: File

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_CANCELED) {
            File(fileUtil.currentPhotoPath).delete()
        } else {
            imagesViewModel.refreshFiles()
        }
    }

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(15)) { uris ->
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                val photoFile: File? = try {
                    fileUtil.createImageFile(imagesViewModel.getCurrentRoot())
                } catch (ex: IOException) {
                    null
                }
                val byteArray = fileUtil.readBytesFromUri(uri)
                byteArray?.let { photoFile?.writeBytes(it) }
            }
            imagesViewModel.refreshFiles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        bundledMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bundledMainActivity.root)

        ViewCompat.setOnApplyWindowInsetsListener(bundledMainActivity.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Timber.plant(Timber.DebugTree())
        Timber.tag(DEBUG_TAG).d("* * * Started App * * *")

        fileUtil = FileUtil(this, applicationContext)
        biometricUtil = BiometricUtil(this)
        rootDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        imagesViewModel = ViewModelProvider(this, ViewModelFactory.getInstance())[ImagesViewModel::class.java]

        // Prevents returning to root dir on device rotation
        if (imagesViewModel.getCurrentRoot() == null) { imagesViewModel.setRootDir(rootDir) }

        setupObservers()
        setupRecyclerView()
        setupOnBackPressed()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bundledMainActivity.clImageOptions.isShown ||
                    bundledMainActivity.clDirectoryOptions.isShown) {
                    toggleImageLongClickOptions(false)
                    toggleDirectoryLongClickOptions(false)
                    return
                }
                if (imagesViewModel.getCurrentRoot()?.name == Environment.DIRECTORY_PICTURES) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
                else {
                    imagesViewModel.setRootDir(imagesViewModel.getCurrentRoot()?.parentFile)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
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
            R.id.miChangeSpan -> {
                handleChangeSpanClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleTakePhotoClick() {
        fileUtil.dispatchTakePictureIntent(imagesViewModel.getCurrentRoot(), takePictureLauncher)
    }

    private fun handleImportPhotoClick() {
        fileUtil.dispatchImportImagesIntent(pickMultipleMedia)
    }

    private fun handleCreateNewFolderClick() {
        fileUtil.showNewFolderAlert(imagesViewModel, imagesViewModel.getCurrentRoot())
    }

    private fun handleChangeSpanClick() {
        imagesViewModel.setSpan()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupObservers() {
        imagesViewModel.filesListLiveData.observe(this) { imagesList ->
            Timber.tag(DEBUG_TAG).d("Total Files: ${imagesList.size}")
            customImageAdapter.notifyDataSetChanged()
        }

        imagesViewModel.rootDirLiveData.observe(this) {
            supportActionBar?.title = it.name
        }

        imagesViewModel.spanCountLiveData.observe(this) {
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        // Setting RV
        recyclerView = bundledMainActivity.rvImagesRecycler.apply {
            customImageAdapter = CustomImageAdapter(imagesViewModel)
            layoutManager = GridLayoutManager(context, imagesViewModel.getSpan())
            adapter = customImageAdapter
            itemAnimator = DefaultItemAnimator()
        }

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
                handleOnDirectoryLongClick(fileLongClicked)
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

        if (dir.isDirectoryPwdLocked(this))
            fileUtil.showEnterPasswordAlert(imagesViewModel, dir)
        if (dir.isDirectoryBiometricLocked(this))
            biometricUtil.showBiometricPrompt(imagesViewModel, dir, PromptType.UNLOCK)
        else if (!dir.isDirectoryLocked(this))
            imagesViewModel.setRootDir(dir)
    }

    private fun handleOnDirectoryLongClick(dir: File) {
        toggleImageLongClickOptions(false)
        toggleDirectoryLongClickOptions(true)

        bundledMainActivity.ivDeleteDirectory.setOnClickListener {
            when (dir.isDirectoryLocked(this)) {
                true -> fileUtil.showCantDeleteDirectoryAlert()
                else -> {
                    fileUtil.showDeleteDirectoryAlert(imagesViewModel, dir)
                    toggleDirectoryLongClickOptions(false)
                }
            }
        }

        bundledMainActivity.ivChangeDirColor.setOnClickListener {
            fileUtil.showChangeDirectoryColorAlert(imagesViewModel, dir)
            toggleDirectoryLongClickOptions(false)
        }

        bundledMainActivity.ivLockDirectory.setOnClickListener {
            if (dir.isDirectoryPwdLocked(this))
                fileUtil.showDeletePasswordAlert(imagesViewModel, dir)
            if (dir.isDirectoryBiometricLocked(this))
                biometricUtil.showBiometricPrompt(imagesViewModel, dir, PromptType.DELETE)
            else if (!dir.isDirectoryLocked(this))
                fileUtil.showChooseLockTypeAlert(imagesViewModel, dir)
            toggleDirectoryLongClickOptions(false)
        }
    }

    private fun toggleImageLongClickOptions(showOptions: Boolean) {
        bundledMainActivity.clImageOptions.visibility = if (showOptions) View.VISIBLE else View.GONE
    }

    private fun toggleDirectoryLongClickOptions(showOptions: Boolean) {
        bundledMainActivity.clDirectoryOptions.visibility = if (showOptions) View.VISIBLE else View.GONE
    }
}
