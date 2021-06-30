package com.nitish.sensorcast.helpers

import java.text.SimpleDateFormat
import java.util.*

fun Long.toTime(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm a")
    return formatter.format(cal.time).toString().toUpperCase()
}