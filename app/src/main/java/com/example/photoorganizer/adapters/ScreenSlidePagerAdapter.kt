package com.example.photoorganizer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
import com.example.photoorganizer.ext.setViewImage
import com.example.photoorganizer.viewmodel.ImagesViewModel
import java.io.File

class ScreenSlidePagerAdapter(private val viewModel: ImagesViewModel) : RecyclerView.Adapter<ScreenSlidePagerAdapter.PageViewHolder>(){
    var onShareClick: ((Int) -> Unit)? = null
    var onDeleteClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_screen_slide_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        viewModel.imagesListLiveData.value?.get(position)?.let { holder.setImage(it) }
    }

    override fun getItemCount(): Int = viewModel.imagesListLiveData.value?.size ?: 0

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImagePage)
        private val imgShare: ImageView = itemView.findViewById(R.id.ivFsShareImage)
        private val imgDelete: ImageView = itemView.findViewById(R.id.ivFsDeleteImage)

        init {
            imgShare.setOnClickListener {
                onShareClick?.invoke(adapterPosition)
            }

            imgDelete.setOnClickListener {
                onDeleteClick?.invoke(adapterPosition)
            }
        }

        fun setImage(file: File) {
            image.setViewImage(file)
        }
    }
}