package com.example.photoorganizer.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoorganizer.R
import java.io.File

class CustomImageAdapter(private val pictureDirectory: Array<File>) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_holder, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.setImageFromPath(pictureDirectory[position].absolutePath)
    }

    override fun getItemCount(): Int {
        return pictureDirectory.size
    }


    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImageHolder)

        fun setImageFromPath(imgFile: String) {
            val pictureBitmap = BitmapFactory.decodeFile(imgFile)
            image.setImageBitmap(pictureBitmap)
        }
    }
}