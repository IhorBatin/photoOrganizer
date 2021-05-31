package com.example.photoorganizer.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R
import com.example.photoorganizer.utils.DEBUG_TAG
import com.example.photoorganizer.viewmodel.ImagesViewModel
import kotlinx.android.synthetic.main.item_image_holder.view.*
import timber.log.Timber
import java.io.File

class CustomImageAdapter(private val viewModel: ImagesViewModel) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

    // private var pictureDirectory: Array<File> = viewModel.imageListLiveData.value as Array<File>
    var onImageClick: ((File) -> Unit)? = null
    var onImageLongClick: ((File) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_holder, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        viewModel.imageListLiveData.value?.get(position)?.let { holder.setImageFromPath(it.toUri()) }
    }

    override fun getItemCount(): Int {
        return viewModel.imageListLiveData.value?.size ?: 0
    }

    /*fun updateImagesList() {
        pictureDirectory = viewModel.imageListLiveData.value as Array<File>
        notifyDataSetChanged()
    }*/

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.ivImageHolder

        init {
            image.setOnClickListener {
                viewModel.imageListLiveData.value?.get(adapterPosition)?.let { file ->
                    onImageClick?.invoke(file)
                }
            }
            image.setOnLongClickListener {
                viewModel.imageListLiveData.value?.get(adapterPosition)?.let { file ->
                    onImageLongClick?.invoke(file)
                }

                return@setOnLongClickListener true // Have to return value
            }
        }

        /** Uri seems to be better format to use than bitmap */
        fun setImageFromPath(imgFile: Uri) {
            Glide
                .with(image.context)
                .load(imgFile)
                .centerCrop()
                .into(image)
        }

    }
}