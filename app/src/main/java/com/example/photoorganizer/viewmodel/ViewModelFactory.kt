package com.example.photoorganizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        with(modelClass){
            when {
                isAssignableFrom(ImagesViewModel::class.java) -> ImagesViewModel.getInstance()
                else -> throw IllegalArgumentException("Unknown viewModel class $modelClass")
            }
        } as T

    companion object {
        private var instance : ViewModelFactory? = null
        fun getInstance() =
            instance ?: synchronized(ViewModelFactory::class.java) {
                instance ?: ViewModelFactory().also { instance = it }
            }
    }
}