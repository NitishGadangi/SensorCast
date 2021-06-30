package com.nitish.sensorcast.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.FragmentBluetoothDevicesBinding
import com.nitish.sensorcast.models.Status

class BluetoothDevicesFragment : Fragment(R.layout.fragment_bluetooth_devices) {

    private val viewModel by lazy {
        (activity as SensorActivity).viewModel
    }

    lateinit var binding: FragmentBluetoothDevicesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentBluetoothDevicesBinding.bind(view)
        setUpViews()
        setUpObservers()
        setUpListeners()
    }

    private fun setUpObservers() {
        viewModel.btMac.observe(viewLifecycleOwner, {
            binding.btnConnect.isEnabled = it != ""
            binding.tvBtInstruction.text = if (it == "")
                "Select a Device from Above list"
            else
                "Selected Device with Mac : $it"
        })
    }

    private fun setUpViews() {
        val devices = viewModel.getBluetoothDevices()
        "Bluetooth Devices (${devices.size})".also {
            binding.tvDevicesCount.text = it
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, devices)
        binding.listDevices.adapter = adapter

    }

    private fun setUpListeners() {
        binding.listDevices.setOnItemClickListener { adapterView, view, i, l ->
            viewModel.updateMac((view as TextView).text.toString())
            activity?.onBackPressed()
        }

        binding.btnClose.setOnClickListener {
            viewModel.disconnect()
        }

        binding.btnRefresh.setOnClickListener {
            setUpViews()
        }

        binding.btnConnect.setOnClickListener {
            viewModel.establishBT()
        }
    }

}