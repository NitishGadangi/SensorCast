package com.nitish.sensorcast.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       viewModel.bluetoothFragmentFisiblity.postValue(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onPause() {
        viewModel.bluetoothFragmentFisiblity.postValue(false)
        super.onPause()
    }

    private fun setUpObservers() {
        viewModel.btMac.observe(viewLifecycleOwner, {
            binding.btnConnect.isEnabled = it != ""
            binding.tvBtInstruction.text = if (it == "")
                "select a Device from Above list"
            else
                "selected Device with Mac : $it"
        })

        viewModel.isBtConnected.observe(viewLifecycleOwner, {
            binding.btnClose.visibility =
                if (it == Status.CONNECTED) View.VISIBLE else View.INVISIBLE
        })
    }

    private fun setUpViews() {
        val devices = viewModel.getBluetoothDevices()
        "Bluetooth Devices (${devices.size})".also {
            binding.tvDevicesCount.text = it
        }
        if (devices.isEmpty()) "check your device bluetooth status and click on refresh".also {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, devices)
        binding.listDevices.adapter = adapter

    }

    private fun setUpListeners() {
        binding.listDevices.setOnItemClickListener { adapterView, view, i, l ->
            viewModel.updateMac((view as TextView).text.toString())
        }

        binding.btnClose.setOnClickListener {
            viewModel.disconnectBtDevice()
        }

        binding.btnRefresh.setOnClickListener {
            setUpViews()
        }

        binding.btnConnect.setOnClickListener {
            if (viewModel.isBtConnected.value == Status.CONNECTED) {
                viewModel.disconnectBtDevice().also {
                    viewModel.connectBtDevice()
                }
            } else
                viewModel.connectBtDevice()
        }
    }

}