package com.example.photoorganizer

import android.app.Application
import timber.log.Timber

class PhotoOrganizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
