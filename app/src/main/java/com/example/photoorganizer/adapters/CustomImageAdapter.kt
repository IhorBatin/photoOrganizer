package com.example.photoorganizer.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R
import java.io.File

class CustomImageAdapter(private val pictureDirectory: Array<File>) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_holder, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.setImageFromPath(pictureDirectory[position].toUri())
    }

    override fun getItemCount(): Int {
        return pictureDirectory.size
    }


    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImageHolder)

        /** Uri seems to be better format to use than bitmap */
        fun setImageFromPath(imgFile: Uri) {
            //val pictureBitmap = BitmapFactory.decodeFile(imgFile)
            //image.setImageBitmap(pictureBitmap)

            Glide
                .with(image.context)
                .load(imgFile)
                .centerCrop()
                .into(image)
            //image.setImageURI(imgFile)
            image.setOnClickListener {
                Toast.makeText(image.context, "Clicked: $imgFile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}