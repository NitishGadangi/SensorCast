package com.nitish.sensorcast.helpers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SensorDetails(
    val name: String,
    val description: String,
    val vendor: String,
    val stringType: String,
    val type: Int,
    val resolution: Float,
    val range: Float,
    val units: String
): Parcelable
