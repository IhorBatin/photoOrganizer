package com.example.photoorganizer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photoorganizer.repository.ImagesRepository
import java.io.File

class ImagesViewModel : ViewModel() {
    val imageListLiveData: MutableLiveData<Array<out File>> = MutableLiveData()


    fun getFilesByDate(rootFile: File?, desc: Boolean = false) {
        imageListLiveData.value = ImagesRepository.fetchAllFilesByDate(rootFile, desc)
    }
}