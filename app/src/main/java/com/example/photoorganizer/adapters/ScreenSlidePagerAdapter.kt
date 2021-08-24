package com.example.photoorganizer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R
import java.io.File

class ScreenSlidePagerAdapter(private val imagesList: Array<out File>) : RecyclerView.Adapter<ScreenSlidePagerAdapter.PageViewHolder>(){
    override fun getItemCount(): Int = imagesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_screen_slide_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.setImage(imagesList[position])
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivImagePage)

        fun setImage(file: File) {
            Glide
                .with(image.context)
                .load(file)
                .into(image)
        }
    }
}