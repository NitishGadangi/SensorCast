package com.nitish.sensorcast.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nitish.sensorcast.repository.SharedPrefManager

class SensorViewModelProviderFactory(
    private val application: Application,
    private val sharedPrefManager: SharedPrefManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SensorViewModel(application, sharedPrefManager) as T
    }

}