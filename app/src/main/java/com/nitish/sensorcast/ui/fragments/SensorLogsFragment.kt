package com.nitish.sensorcast.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.FragmentSensorLogsBinding
import com.nitish.sensorcast.ui.SensorActivity

class SensorLogsFragment : Fragment(R.layout.fragment_sensor_logs) {

    lateinit var binding: FragmentSensorLogsBinding

    private val viewModel by lazy {
        (activity as SensorActivity).viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSensorLogsBinding.bind(view)

        setUpViews()
    }

    private fun setUpViews() {
        viewModel.inputStream.observe(viewLifecycleOwner, {
            binding.textView.text = it
        })

        binding.btnClearAll.setOnClickListener {
            viewModel.clearLogs()
        }
    }
}