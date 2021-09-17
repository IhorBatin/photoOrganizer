package com.example.photoorganizer.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.ScreenSlidePagerAdapter
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.utils.OPEN_AT_POS
import com.example.photoorganizer.utils.ZoomOutPageTransformer
import com.example.photoorganizer.viewmodel.ImagesViewModel
import com.example.photoorganizer.viewmodel.ViewModelFactory
import timber.log.Timber

class ScreenSlidePagerActivity : FragmentActivity() {

    private lateinit var imagesViewModel: ImagesViewModel
    private lateinit var viewPager: ViewPager2
    private var openImageAtPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)
        imagesViewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(ImagesViewModel::class.java)
        imagesViewModel.getImagesForViewPager(imagesViewModel.rootDirLiveData.value)

        Timber.tag(DEBUG_TAG).d("*** Image Slider")
        Timber.tag(DEBUG_TAG).d("Root Directory: ${imagesViewModel.rootDirLiveData.value?.name}")
        Timber.tag(DEBUG_TAG).d("Num Images: ${imagesViewModel.imagesListLiveData.value?.size}")

        openImageAtPosition = intent.getIntExtra(OPEN_AT_POS, 0)

        setupObservers()
        setupViewPager()
    }

    private fun setupObservers() {
        imagesViewModel.imagesListLiveData.observe(this, { imagesList ->
            Timber.tag(DEBUG_TAG).d("setupObservers() imagesList size: ${imagesList.size}")
            viewPager.adapter?.notifyDataSetChanged()
        })
    }

    private fun setupViewPager() {
        Timber.tag(DEBUG_TAG).d("setupViewpager()")
        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)

        // The pager adapter, which provides the pages to the view pager widget.
        viewPager.adapter = ScreenSlidePagerAdapter(imagesViewModel)
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        viewPager.setCurrentItem(openImageAtPosition, false)
    }
}