package com.nitish.sensorcast.ui

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.ActivityBaseBinding
import com.nitish.sensorcast.databinding.FragmentAppBarBinding
import com.nitish.sensorcast.helpers.SensorDetails
import com.nitish.sensorcast.models.Status
import com.nitish.sensorcast.repository.SharedPrefManager

class SensorActivity : AppCompatActivity() {

    lateinit var binding: ActivityBaseBinding

    lateinit var appBarBinding: FragmentAppBarBinding

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentByTag("navHostFragment") as NavHostFragment
    }

    val viewModel: SensorViewModel by lazy {
        val sensorViewModelProviderFactory = SensorViewModelProviderFactory(
            application,
            SharedPrefManager.getInstance(applicationContext)
        )
        ViewModelProvider(this, sensorViewModelProviderFactory).get(SensorViewModel::class.java)
    }

    private val sensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appBarBinding = binding.fragmentAppBar

        binding.bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        setUpObservers()
        setUpListeners()
    }

    private fun setUpObservers() {
        viewModel.isBtConnected.observe(this, {
            if (it == Status.CONNECTED) {
                "${it.msg} | ${viewModel.btMac.value}".also { appBarBinding.tvBtStatus.text = it }
            } else {
                appBarBinding.tvBtStatus.text = it.msg
            }
            appBarBinding.tvBtStatus.setBackgroundColor(resources.getColor(it.color))
        })
    }

    private fun setUpListeners() {
        appBarBinding.btnBluetooth.setOnClickListener {
            navHostFragment.findNavController().navigate(R.id.bluetoothDevicesFragment)
        }
    }
}