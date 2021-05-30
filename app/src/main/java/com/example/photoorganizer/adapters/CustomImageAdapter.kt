package com.example.photoorganizer.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R
import kotlinx.android.synthetic.main.item_image_holder.view.*
import java.io.File

class CustomImageAdapter(
    private var pictureDirectory: Array<File>
) : RecyclerView.Adapter<CustomImageAdapter.ImageViewHolder>() {

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

    fun updateImagesList(newList: Array<File>) {
        pictureDirectory = newList
        notifyDataSetChanged()
    }

    fun getPicturesUri(position: Int) : Uri = pictureDirectory[position].toUri()

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.ivImageHolder

        init {
            image.setOnClickListener {
                //Toast.makeText(itemView.context, "YAY I work $adapterPosition", Toast.LENGTH_SHORT).show()

            }
            image.setOnLongClickListener(View.OnLongClickListener {
                Toast.makeText(itemView.context, "YAY I long work $adapterPosition", Toast.LENGTH_SHORT).show()
                val uri =
                val sendIntent = Intent()
                val shareIntent: Intent = Intent.createChooser(sendIntent, null)
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_STREAM, )
                sendIntent.type = "image/jpeg"
                startActivity(itemView.context, shareIntent, null)


                return@OnLongClickListener true // Requires to return boolean
            })
        }


        /** Uri seems to be better format to use than bitmap */
        fun setImageFromPath(imgFile: Uri) {
            Glide
                .with(image.context)
                .load(imgFile)
                .centerCrop()
                .into(image)
            //image.setImageURI(imgFile)
            /*image.setOnClickListener {
                Toast.makeText(image.context, "Clicked: $imgFile", Toast.LENGTH_SHORT).show()
            }*/
        }

    }
}