package com.nitish.sensorcast.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.ActivityBaseBinding
import com.nitish.sensorcast.databinding.FragmentAppBarBinding
import com.nitish.sensorcast.helpers.SensorDetails
import com.nitish.sensorcast.models.Status
import com.nitish.sensorcast.repository.SharedPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SensorActivity : AppCompatActivity(), SensorEventListener {

    lateinit var binding: ActivityBaseBinding

    lateinit var appBarBinding: FragmentAppBarBinding

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentByTag("navHostFragment") as NavHostFragment
    }

    private var sensorEvent : SensorEvent? = null

    val viewModel: SensorViewModel by lazy {
        val sensorViewModelProviderFactory = SensorViewModelProviderFactory(
            application,
            SharedPrefManager.getInstance(applicationContext)
        )
        ViewModelProvider(this, sensorViewModelProviderFactory).get(SensorViewModel::class.java)
    }

    val sensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if(it.sensor == viewModel.currentSensor.value){
                sensorEvent = event
                viewModel.currentSensorValues.postValue(event.values)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }

    fun registerSensor(){
        viewModel.currentSensor.observe(this, {
            it?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        })
    }

    fun unregisterSensor(){
        viewModel.currentSensor.postValue(null)
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appBarBinding = binding.fragmentAppBar

        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        setUpObservers()
        setUpListeners()

        startCastListener()
    }

    private fun startCastListener() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (true){
                if (viewModel.isCastEnabled.value == true) sensorEvent?.let {
                    viewModel.sendData(it.values)
                }
                delay(500)
            }
        }
    }

    private fun setUpObservers() {
        viewModel.isBtConnected.observe(this, {
            updateHeader()
        })

        viewModel.isCastEnabled.observe(this, {
            if (it == true) {
                "${viewModel.isBtConnected.value?.msg} | ${viewModel.btName.value} | CASTING".also { appBarBinding.tvBtStatus.text = it }
            } else {
                updateHeader()
            }
        })

        viewModel.bluetoothFragmentFisiblity.observe(this, {visible ->
            if(visible) {
                appBarBinding.btnBluetooth.setImageResource(R.drawable.ic_close)
            }else {
                appBarBinding.btnBluetooth.setImageResource(R.drawable.ic_bluetooth)
            }
        })
    }

    private fun updateHeader() {
        viewModel.isBtConnected.value?.also {
            if (it == Status.CONNECTED) {
                "${it.msg} | ${viewModel.btName.value}".also { appBarBinding.tvBtStatus.text = it }
            } else {
                appBarBinding.tvBtStatus.text = it.msg
            }
            appBarBinding.tvBtStatus.setBackgroundColor(resources.getColor(it.color))
        }
    }

    private fun setUpListeners() {
        appBarBinding.btnBluetooth.setOnClickListener {
            if(viewModel.bluetoothFragmentFisiblity.value == false)
                navHostFragment.findNavController().navigate(R.id.bluetoothDevicesFragment)
            else
                onBackPressed()
        }
    }
}