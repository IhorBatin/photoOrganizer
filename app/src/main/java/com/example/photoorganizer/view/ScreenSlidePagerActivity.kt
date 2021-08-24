package com.example.photoorganizer.view

import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.ScreenSlidePagerAdapter
import com.example.photoorganizer.utils.ZoomOutPageTransformer
import com.example.photoorganizer.viewmodel.ImagesViewModel
import java.io.File

class ScreenSlidePagerActivity : FragmentActivity() {

    private val imagesViewModel: ImagesViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var curDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_slide)

        //curDirectory =  getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        imagesViewModel.getImagesForViewPager(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
        //setupViewPager()
        setupObservers()
    }

    private fun setupObservers() {
        imagesViewModel.imagesListLiveData.observe(this, { imagesList ->
            setupViewPager(imagesList)
        })
    }

    private fun setupViewPager(imagesList: Array<out File>) {
        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)

        // The pager adapter, which provides the pages to the view pager widget.
        viewPager.adapter = ScreenSlidePagerAdapter(imagesList)
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        //viewPager.setCurrentItem(2, false)
    }
}