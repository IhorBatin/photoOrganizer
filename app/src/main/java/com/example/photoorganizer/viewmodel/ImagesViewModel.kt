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
        filesListLiveData.value = ImagesRepository.getFilesListDirectoriesFirst(ImagesRepository.fetchAllFilesByDate(directory))
        imagesListLiveData.value = ImagesRepository.getListOfImagesOnly(ImagesRepository.fetchAllFilesByDate(directory))
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

    fun gtePositionForImageSlider(clickPosition: Int) : Int {
        return ImagesRepository.getImagePositionIgnoringDirectories(clickPosition, getCurrentRoot())
    }

    companion object {
        private var instance : ImagesViewModel? = null
        fun getInstance() =
            instance ?: synchronized(ImagesViewModel::class.java){
                instance ?: ImagesViewModel().also { instance = it }
            }
    }
}