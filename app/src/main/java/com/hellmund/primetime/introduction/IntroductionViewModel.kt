package com.hellmund.primetime.introduction

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hellmund.primetime.utils.DownloadManager
import org.jetbrains.anko.doAsync

class IntroductionViewModel : ViewModel() {

    private val _posterUrls = MutableLiveData<List<String>>()
    val posterUrls: LiveData<List<String>> = _posterUrls

    init {
        loadPosters()
    }

    private fun loadPosters() {
        doAsync {
            val results = DownloadManager.downloadMostPopularMoviePosters()
            _posterUrls.postValue(results)
        }
    }

}
