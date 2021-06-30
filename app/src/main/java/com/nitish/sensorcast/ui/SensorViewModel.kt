package com.nitish.sensorcast.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nitish.sensorcast.helpers.toTime
import com.nitish.sensorcast.models.Status
import com.nitish.sensorcast.repository.SharedPrefManager
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class SensorViewModel(
    application: Application,
    private val sharedPrefManager: SharedPrefManager
) : AndroidViewModel(application) {

    val btMac = MutableLiveData("")

    val isBtConnected = MutableLiveData(Status.DISCONNECTED)

    val inputStream = MutableLiveData(sharedPrefManager.sensorLog)

    private var btSocket: BluetoothSocket? = null

    private var btMonitorJob: Job? = null

    private var inputStreamJob: Job? = null

    private var oldData = ""

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    fun getBluetoothDevices(): List<String> {
        if (btAdapter == null) return listOf()
        return btAdapter.bondedDevices.toList().map { bt -> "${bt.name}\n${bt.address}" }
    }

    private fun getMainBtDeviceAddress(base: String): String {
        return base.substring(base.length - 17)
    }

    fun updateMac(mac: String) {
        disconnect()
        isBtConnected.postValue(Status.DISCONNECTED)
        btMac.postValue(getMainBtDeviceAddress(mac))
    }

    fun establishBT(): String {
        if (btMac.value?.length == MAC_SIZE) {
            if (btSocket == null || btSocket?.isConnected == false) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        isBtConnected.postValue(Status.CONNECTING)
                        val device = btAdapter.getRemoteDevice(btMac.value)
                        btSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID)
                        btAdapter.cancelDiscovery()
                        btSocket?.connect()
                        isBtConnected.postValue(Status.CONNECTED)
                        monitorConnection()
                        monitorInputStream()
                    } catch (exp: IOException) {
                        isBtConnected.postValue(Status.DISCONNECTED)
                    }
                }
            } else {
                return "Socket not found"
            }
        } else {
            return "INVALID MAC FOUND"
        }
        return "ESTABLISHING CONNECTION"
    }

    private fun monitorConnection(){
        btMonitorJob?.cancel()
        btMonitorJob = GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                if (btSocket == null || btSocket?.isConnected == false) {
                    isBtConnected.postValue(Status.DISCONNECTED)
                    break
                }
                delay(1000)
            }
        }
    }

    private fun monitorInputStream(){
        inputStreamJob?.cancel()
        inputStreamJob = GlobalScope.launch(Dispatchers.IO) {
            val stream = btSocket?.inputStream
            while (stream != null && btSocket?.isConnected == true) {
                try {
                    if (stream.available() > 0){
                        val buffer = ByteArray(256)
                        val size = stream.read(buffer)
                        val response = String(buffer, 0, size)
                        response.let {
                            inputStream.postValue(sharedPrefManager.updateSensorLog("${System.currentTimeMillis().toTime()} > $it"))
                        }
                    }
                }catch (exp: IOException){
                    Log.i("BLUE EXP", "$exp Stream error")
                }
                delay(500)
            }
        }
    }

    fun disconnect() {
        btSocket = btSocket.let {
            it?.close()
            null
        }
        btMac.postValue("")
        isBtConnected.postValue(Status.DISCONNECTED)
    }

    fun sendData(data: String) {
        if(data == oldData) return
        oldData = data
        if(btSocket != null && btSocket?.isConnected == true){
            try {
                btSocket?.outputStream?.write(data.toByteArray(charset = Charset.defaultCharset()))
            }catch (exp: IOException){
                isBtConnected.postValue(Status.DISCONNECTED)
            }
        }
    }

    companion object {
        val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val MAC_SIZE = 17
    }
}