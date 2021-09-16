package com.example.photoorganizer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photoorganizer.repository.ImagesRepository
import java.io.File

class ImagesViewModel : ViewModel() {
    val filesListLiveData: MutableLiveData<Array<out File>> = MutableLiveData()
    val imagesListLiveData: MutableLiveData<Array<out File>> = MutableLiveData()
    val rootDirLiveData: MutableLiveData<File> = MutableLiveData()

    private fun updateFiles(directory: File?) {
        //imageListLiveData.value = ImagesRepository.fetchAllFilesByDate(directory)
        filesListLiveData.value = ImagesRepository.getFilesListDirectoriesFirst(ImagesRepository.fetchAllFilesByDate(directory))
    }

    fun getFilesByDate(rootFile: File?, desc: Boolean = false) {
        filesListLiveData.value = ImagesRepository.fetchAllFilesByDate(rootFile, desc)
    }

    fun getImagesForViewPager(directory: File?, desc: Boolean = false) {
        imagesListLiveData.value = ImagesRepository.getListOfImagesOnly(ImagesRepository.fetchAllFilesByDate(directory))
    }

    fun setRootDir(root: File?) {
        rootDirLiveData.value = root
        updateFiles(root)
    }

    fun refreshFiles() {
        updateFiles(rootDirLiveData.value)
    }

    fun getCurrentRoot() : File? = rootDirLiveData.value
}