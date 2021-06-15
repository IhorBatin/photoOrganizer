package com.example.photoorganizer.ext

import android.view.View.VISIBLE
import android.view.View.GONE
import android.widget.TextView

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