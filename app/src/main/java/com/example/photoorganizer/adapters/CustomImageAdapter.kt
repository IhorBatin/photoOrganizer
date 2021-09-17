package com.example.photoorganizer.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R
import com.example.photoorganizer.viewmodel.ImagesViewModel
import java.io.File

class CustomImageAdapter(private val viewModel: ImagesViewModel) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

    // private var pictureDirectory: Array<File> = viewModel.imageListLiveData.value as Array<File>
    var onImageClick: ((Int) -> Unit)? = null
    var onImageLongClick: ((File) -> Unit)? = null

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

/*    fun updateImagesList() {
        val pictureDirectory = viewModel.imageListLiveData.value as Array<File>
        notifyDataSetChanged()
    }*/

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImageHolder)

        init {
            image.setOnClickListener {
                //viewModel.filesListLiveData.value?.get(adapterPosition)?.let { file ->
                    onImageClick?.invoke(adapterPosition)
                //}
            }
            image.setOnLongClickListener {
                viewModel.filesListLiveData.value?.get(adapterPosition)?.let { file ->
                    onImageLongClick?.invoke(file)
                }

                return@setOnLongClickListener true // Have to return value
            }
        }

        fun setImageForFile(file: File) {
            if (file.isDirectory) {
               itemView.findViewById<TextView>(R.id.tvDirTitle).let {
                   it.visibility = View.VISIBLE
                   it.text = file.name
                }
                image.setBackgroundColor(Color.TRANSPARENT)
                Glide
                    .with(image.context)
                    .load(R.drawable.ic_folder)
                    .into(image)
            }
            else {
                itemView.findViewById<TextView>(R.id.tvDirTitle).visibility = View.GONE
                Glide
                    .with(image.context)
                    .load(file)
                    .centerCrop()
                    .into(image)
            }
        }

    }
}