package com.nitish.sensorcast.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

fun Long.toTime(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm a")
    return formatter.format(cal.time).toString().toUpperCase()
}

fun Activity.openBrowser(URL: String) {
    this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
}