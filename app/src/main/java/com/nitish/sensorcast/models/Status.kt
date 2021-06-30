package com.nitish.sensorcast.models

import com.nitish.sensorcast.R

enum class Status(val msg:String, val color: Int, var mac:String = "") {
    CONNECTED("CONNECTED", R.color.green),
    CONNECTING("Connecting...", R.color.grey100),
    DISCONNECTED("NO CONNECTION", R.color.red)
}