package com.example.photoorganizer.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
import com.example.photoorganizer.ext.getDirectoryColor
import com.example.photoorganizer.ext.isDirectoryLocked
import com.example.photoorganizer.ext.setDefaultFolderImage
import com.example.photoorganizer.ext.setViewImage
import com.example.photoorganizer.viewmodel.ImagesViewModel
import java.io.File

class CustomImageAdapter(private val viewModel: ImagesViewModel) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

    var onImageClick: ((Int) -> Unit)? = null
    var onImageLongClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_holder, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        viewModel.filesListLiveData.value?.get(position)?.let { holder.setImageForFile(it) }
    }

    override fun getItemCount(): Int {
        return viewModel.filesListLiveData.value?.size ?: 0
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImageHolder)

        init {
            image.setOnClickListener {
                onImageClick?.invoke(bindingAdapterPosition)
            }
            image.setOnLongClickListener {
                onImageLongClick?.invoke(bindingAdapterPosition)

                return@setOnLongClickListener true // Have to return value
            }
        }

        fun setImageForFile(file: File) {
            if (file.isDirectory) {
                itemView.findViewById<ImageView>(R.id.ivFileLockedIcon).visibility =
                    if (file.isDirectoryLocked(image.context as Activity)) VISIBLE else GONE

                itemView.findViewById<TextView>(R.id.tvDirTitle).let {
                    it.visibility = VISIBLE
                    it.text = file.name
                }
                image.setBackgroundColor(Color.TRANSPARENT)
                image.setDefaultFolderImage()
                image.setColorFilter(file.getDirectoryColor(image.context as Activity))
            }
            else {
                itemView.findViewById<TextView>(R.id.tvDirTitle).visibility = GONE
                itemView.findViewById<ImageView>(R.id.ivFileLockedIcon).visibility = GONE
                image.setColorFilter(Color.TRANSPARENT)
                image.setViewImage(file)
            }
        }

    }
}
