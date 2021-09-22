package com.example.photoorganizer.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.ScreenSlidePagerAdapter
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.utils.FileUtil
import com.example.photoorganizer.utils.OPEN_AT_POS
import com.example.photoorganizer.utils.ZoomOutPageTransformer
import com.example.photoorganizer.viewmodel.ImagesViewModel
import com.example.photoorganizer.viewmodel.ViewModelFactory
import timber.log.Timber

class ScreenSlidePagerActivity : FragmentActivity() {

    private lateinit var imagesViewModel: ImagesViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var sliderAdapter: ScreenSlidePagerAdapter
    private lateinit var fileUtil: FileUtil
    private var openImageAtPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)
        imagesViewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(ImagesViewModel::class.java)
        imagesViewModel.getImagesForViewPager(imagesViewModel.rootDirLiveData.value)
        fileUtil = FileUtil(this, applicationContext)
        openImageAtPosition = intent.getIntExtra(OPEN_AT_POS, 0)

        setupObservers()
        setupViewPager()
        setupSliderListeners()
    }

    private fun setupObservers() {
        imagesViewModel.imagesListLiveData.observe(this, { imagesList ->
            Timber.tag(DEBUG_TAG).d("setupObservers() imagesList size: ${imagesList.size}")
        })
    }

    private fun setupViewPager() {
        Timber.tag(DEBUG_TAG).d("setupViewpager()")
        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)
        // The pager adapter, which provides the pages to the view pager widget.
        sliderAdapter = ScreenSlidePagerAdapter(imagesViewModel)

        viewPager.adapter = sliderAdapter
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        viewPager.setCurrentItem(openImageAtPosition, false)
    }

    private fun setupSliderListeners() {
        sliderAdapter.onShareClick = {  position ->
            imagesViewModel.imagesListLiveData.value?.get(position)?.let {
                fileUtil.dispatchShareImageIntent(it)
            }
        }

        sliderAdapter.onDeleteClick = { position ->
            fileUtil.deleteImageFromSliderAdapter(position, imagesViewModel, sliderAdapter)
            if (imagesViewModel.imagesListLiveData.value.isNullOrEmpty()) onBackPressed()
        }
    }
}