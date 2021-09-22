package com.example.photoorganizer.ext

import android.app.Activity
import android.content.Context
import com.example.photoorganizer.R
import java.io.File

const val SP_PASSWORD = "_PASSWORD"
const val SP_BIO_LOCK = "_BIOMETRICS"

fun File.getDirectoryColor(activity: Activity) : Int {
    return activity.getPreferences(Context.MODE_PRIVATE)
        .getInt(this.path, activity.resources.getColor(R.color.folder_default))
}

fun File.setDirectoryColor(activity: Activity, color: Int) {
    activity.getPreferences(Context.MODE_PRIVATE)
    with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
        putInt(this@setDirectoryColor.path, color)
        apply()
    }
}

fun File.isDirectoryLocked(activity: Activity) : Boolean {
    return isDirectoryPwdLocked(activity) || isDirectoryBiometricLocked(activity)
}

fun File.isDirectoryPwdLocked(activity: Activity) : Boolean {
    return when(getDirectoryPassword(activity)) {
        null -> false
        else -> true
    }
}

fun File.getDirectoryPassword(activity: Activity) : String? {
    val key = this.path + SP_PASSWORD
    return activity.getPreferences(Context.MODE_PRIVATE)
        .getString(key, null)
}

fun File.setDirectoryPassword(activity: Activity, pass: String) {
    val key = this.path + SP_PASSWORD
    activity.getPreferences(Context.MODE_PRIVATE)
    with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
        putString(key, pass)
        apply()
    }
}

fun File.clearDirectoryPassword(activity: Activity) {
    val key = this.path + SP_PASSWORD
    activity.getPreferences(Context.MODE_PRIVATE)
    with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
        remove(key)
        apply()
    }
}

fun File.isDirectoryBiometricLocked(activity: Activity) : Boolean {
    val key = this.path + SP_BIO_LOCK
    return activity.getPreferences(Context.MODE_PRIVATE)
        .getBoolean(key, false)
}

fun File.setDirectoryBiometricLocked(activity: Activity, locked: Boolean) {
    val key = this.path + SP_BIO_LOCK
    activity.getPreferences(Context.MODE_PRIVATE)
    with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
        putBoolean(key, locked)
        apply()
    }
}