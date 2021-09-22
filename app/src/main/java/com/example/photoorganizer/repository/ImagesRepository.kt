package com.example.photoorganizer.repository

import java.io.File

object ImagesRepository {

    fun fetchAllFilesByDate(rootFile: File?, desc: Boolean = false) : Array<out File>? {
        return sortFilesByDate(fetchAllFilesFromDir(rootFile), desc)
    }

    private fun fetchAllFilesFromDir(rootFile: File?) : Array<out File>? {
        return rootFile?.listFiles()
    }

    private fun sortFilesByDate(filesList: Array<out File>?, desc: Boolean) : Array<out File>? {
        when (desc) {
            true -> filesList?.sortBy {
                it.lastModified()
            }
            false -> filesList?.sortByDescending {
                it.lastModified()
            }
        }
        return filesList
    }

    /**
     * Sorts all files in a way such that all directories are at the front.
     * Secondary order depends on [sortedFiles] order
     */
    fun getFilesListDirectoriesFirst(sortedFiles: Array<out File>?) : Array<out File> {
        val dirsList = mutableListOf<File>()
        val picturesList = mutableListOf<File>()

        sortedFiles?.forEach { file ->
            if (file.isDirectory) dirsList.add(file)
            else picturesList.add(file)
        }
        return (dirsList + picturesList).toMutableList().toTypedArray()
    }

    /**
     * Sorts all files in a way that directories will not be returned, only files
     */
    fun getListOfImagesOnly(sortedFiles: Array<out File>?) : Array<out File> {
        val picturesList = mutableListOf<File>()
        sortedFiles?.forEach { file ->
            if (file.isFile) picturesList.add(file)
        }
        return picturesList.toTypedArray()
    }

    fun getImagePositionIgnoringDirectories(clickPosition: Int, dir: File?): Int {
        var numOfDirs = 0
        fetchAllFilesFromDir(dir)?.forEach {
            if (it.isDirectory) numOfDirs++
        }
        return clickPosition - numOfDirs
    }

}