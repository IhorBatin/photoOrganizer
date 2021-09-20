package com.example.photoorganizer.ext

import android.view.View.VISIBLE
import android.view.View.GONE
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.photoorganizer.R

fun TextView.toggleErrorMessage(error: Int = 0, show: Boolean = true) {
    when {
        show -> {
            this.visibility = VISIBLE
            this.text = this.context.getString(error)
        }
        else -> {
            this.visibility = GONE
            this.text = ""
        }
    }
}

fun ImageView.setDefaultImage() {
    Glide
        .with(this.context)
        .load(R.drawable.ic_folder_default)
        .into(this)
}