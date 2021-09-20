package com.example.photoorganizer.ext

import android.app.Activity
import android.content.Context
import com.example.photoorganizer.R
import java.io.File

fun File.getDirectoryColor(activity: Activity) : Int {
    return activity.getPreferences(Context.MODE_PRIVATE)
        .getInt(this.path, R.color.folder_default)
}

fun File.setDirectoryColor(activity: Activity, color: Int) {
    activity.getPreferences(Context.MODE_PRIVATE)
    with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
        putInt(this@setDirectoryColor.path, color)
        apply()
    }
}