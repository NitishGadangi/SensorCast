package com.nitish.sensorcast.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nitish.sensorcast.helpers.SensorDetails
import com.nitish.sensorcast.helpers.round
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

    val bluetoothFragmentFisiblity = MutableLiveData(false)

    val btMac = MutableLiveData("")

    val btName = MutableLiveData("")

    val isBtConnected = MutableLiveData(Status.DISCONNECTED)

    val inputStream = MutableLiveData(sharedPrefManager.sensorLog)

    val isCastEnabled = MutableLiveData(false)

    val currentSensor = MutableLiveData<Sensor?>(null)

    val currentSensorValues = MutableLiveData(floatArrayOf())

    var isDataBeingSent = false

    private var btSocket: BluetoothSocket? = null

    private var btMonitorJob: Job? = null

    private var inputStreamJob: Job? = null

    private var oldData = ""

    private val btAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val sensorManager by lazy {
        application.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
    }

    fun getBluetoothDevices(): List<String> {
        if (btAdapter == null) return listOf()
        return btAdapter.bondedDevices.toList().map { bt -> "${bt.name}\n${bt.address}" }
    }

    private fun getMainBtDeviceAddress(base: String): String {
        return base.substring(base.length - 17)
    }

    private fun getMainBtDeviceName(base: String): String {
        return base.substring(0, base.length - 18)
    }

    fun updateMac(mac: String) {
        disconnectBtDevice()
        isBtConnected.postValue(Status.DISCONNECTED)
        btMac.postValue(getMainBtDeviceAddress(mac))
        btName.postValue(getMainBtDeviceName(mac))
    }

    fun connectBtDevice(): String {
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

    private fun monitorConnection() {
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

    private fun monitorInputStream() {
        inputStreamJob?.cancel()
        inputStreamJob = GlobalScope.launch(Dispatchers.IO) {
            val stream = btSocket?.inputStream
            while (stream != null && btSocket?.isConnected == true) {
                try {
                    if (stream.available() > 0) {
                        val buffer = ByteArray(256)
                        val size = stream.read(buffer)
                        val response = String(buffer, 0, size)
                        response.let {
                            inputStream.postValue(
                                sharedPrefManager.updateSensorLog(
                                    "${
                                        System.currentTimeMillis().toTime()
                                    } > $it"
                                )
                            )
                        }
                    }
                } catch (exp: IOException) {
                    Log.i("BLUE EXP", "$exp Stream error")
                }
                delay(500)
            }
        }
    }

    fun disconnectBtDevice() {
        btSocket = btSocket.let {
            it?.close()
            null
        }
        btMac.postValue("")
        btName.postValue("")
        isBtConnected.postValue(Status.DISCONNECTED)
        isCastEnabled.postValue(false)
    }

    fun sendData(data: String) {
        if (data == oldData) return
        oldData = data
        if (btSocket != null && btSocket?.isConnected == true) {
            isDataBeingSent = true
            try {
                btSocket?.outputStream?.apply {
                    flush()
                    write(data.toByteArray(charset = Charset.defaultCharset()))
                }
            } catch (exp: IOException) {
                isBtConnected.postValue(Status.DISCONNECTED)
            }
            isDataBeingSent = false
        }
    }

    fun sendData(data: FloatArray) {
        if(!isDataBeingSent) sendData(data.map { it.round(1) }.joinToString(separator = " "))
    }

    fun clearLogs() {
        sharedPrefManager.clearSensorLog()
        inputStream.postValue(sharedPrefManager.sensorLog)
    }

    private fun getSensorDetails(
        sensorType: Int,
        sensorName: String,
        sensorDesc: String,
        sensorUnits: String
    ): SensorDetails? {
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return null
        return SensorDetails(
            name = sensorName,
            description = sensorDesc,
            vendor = sensor.vendor,
            stringType = sensor.stringType,
            type = sensor.type,
            resolution = sensor.resolution,
            range = sensor.maximumRange,
            units = sensorUnits
        )
    }

    fun getSensorsList(): List<SensorDetails> {
        val result = mutableListOf<SensorDetails>()
        getSensorDetails(
            Sensor.TYPE_ACCELEROMETER,
            "Accelerometer",
            ACCELEROMETER_DESCRIPTION,
            "m/s2"
        )?.let {
            result.add(it)
        }
        getSensorDetails(
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            "Ambient Temperature",
            AMBIENT_TEMPERATURE_DESCRIPTION,
            "°C"
        )?.let {
            result.add(it)
        }
        getSensorDetails(
            Sensor.TYPE_MAGNETIC_FIELD,
            "Magnetic Field",
            MAGNETIC_FIELD_DESCRIPTION,
            "uT"
        )?.let {
            result.add(it)
        }
        getSensorDetails(Sensor.TYPE_GYROSCOPE, "Gyroscope", GYROSCOPE_DESCRIPTION, "rad/s")?.let {
            result.add(it)
        }
        getSensorDetails(Sensor.TYPE_HEART_RATE, "Heart Rate", HEART_RATE_DESCRIPTION, "BPM")?.let {
            result.add(it)
        }
        getSensorDetails(Sensor.TYPE_LIGHT, "Ambient Light", LIGHT_DESCRIPTION, "lux")?.let {
            result.add(it)
        }
        getSensorDetails(Sensor.TYPE_PROXIMITY, "Proximity", PROXIMITY_DESCRIPTION, "cm")?.let {
            result.add(it)
        }
        getSensorDetails(Sensor.TYPE_PRESSURE, "Pressure", PRESSURE_DESCRIPTION, "hPa")?.let {
            result.add(it)
        }
        getSensorDetails(
            Sensor.TYPE_PRESSURE,
            "Relative Humidity",
            HUMIDITY_DESCRIPTION,
            "%"
        )?.let {
            result.add(it)
        }
        return result
    }

    companion object {
        val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val MAC_SIZE = 17
        const val ARDUINO_SETUP_LINK = "https://github.com/NitishGadangi/SensorCast"
        const val ACCELEROMETER_DESCRIPTION =
            "An accelerometer sensor reports the acceleration of the device along the three sensor axes." +
                    "The measured acceleration includes both the physical acceleration (change of velocity) and the gravity." +
                    "The measurement is reported in the x, y, and z fields of sensors_event_t.acceleration."
        const val GYROSCOPE_DESCRIPTION =
            "A gyroscope sensor reports the rate of rotation of the device around the three sensor axes.\n" +
                    "Rotation is positive in the counterclockwise direction (right-hand rule). " +
                    "That is, an observer looking from some positive location on the x, y, or z axis at a device positioned on the origin would" +
                    "report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the standard mathematical " +
                    "definition of positive rotation and does not agree with the aerospace definition of roll."
        const val AMBIENT_TEMPERATURE_DESCRIPTION =
            "This sensor provides the ambient (room) temperature in degrees Celsius."
        const val MAGNETIC_FIELD_DESCRIPTION =
            "A magnetic field sensor (also known as magnetometer) reports the ambient magnetic field, as measured along the three sensor axes."
        const val HEART_RATE_DESCRIPTION =
            "A heart rate sensor reports the current heart rate of the person touching the device."
        const val LIGHT_DESCRIPTION =
            "A light sensor reports the current illumination in SI lux units."
        const val PRESSURE_DESCRIPTION =
            "A pressure sensor (also known as barometer) reports the atmospheric pressure in hectopascal (hPa).\n" +
                    "\n" +
                    "The readings are calibrated using\n" +
                    "- Temperature compensation\n" +
                    "- Factory bias calibration\n" +
                    "- Factory scale calibration"
        const val HUMIDITY_DESCRIPTION =
            "A relative humidity sensor measures relative ambient air humidity and returns a value in percent."
        const val PROXIMITY_DESCRIPTION =
            "A proximity sensor reports the distance from the sensor to the closest visible surface. Note that some proximity sensors only support a binary \"near\" or \"far\" measurement."

    }
}