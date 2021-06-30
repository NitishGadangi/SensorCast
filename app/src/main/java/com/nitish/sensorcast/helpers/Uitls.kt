package com.nitish.sensorcast.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

fun Long.toTime(): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm a")
    return formatter.format(cal.time).toString().toUpperCase()
}

fun Activity.openBrowser(URL: String) {
    this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
}

fun Float.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}