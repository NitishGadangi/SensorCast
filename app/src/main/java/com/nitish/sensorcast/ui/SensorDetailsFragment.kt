package com.nitish.sensorcast.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.FragmentSensorDetailsBinding
import com.nitish.sensorcast.helpers.SensorDetails
import com.nitish.sensorcast.helpers.openBrowser

class SensorDetailsFragment : Fragment(R.layout.fragment_sensor_details) {

    private val viewModel by lazy {
        (activity as SensorActivity).viewModel
    }

    lateinit var binding: FragmentSensorDetailsBinding

    lateinit var sensorDetails: SensorDetails

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSensorDetailsBinding.bind(view)

        sensorDetails = SensorDetailsFragmentArgs.fromBundle(requireArguments()).sensor

        setUpViews()
        setUpObservers()
        setUpListeners()
    }

    private fun setUpViews() {
        binding.tvSensorNameHeading.text = sensorDetails.name
        binding.tvSensorDesc.text = sensorDetails.description
        "Vendor : ${sensorDetails.vendor}".also { binding.tvSensorVendor.text = it }
        "Type : ${sensorDetails.stringType}".also { binding.tvSensorType.text = it }
        "Resolution : ${sensorDetails.resolution} ${sensorDetails.units}".also { binding.tvSensorResolution.text = it }
        "Range : ${sensorDetails.range} ${sensorDetails.units}".also { binding.tvSensorRange.text = it }
        "Units : ${sensorDetails.units}".also { binding.tvSensorUnits.text = it }
    }

    private fun setUpObservers() {

    }

    private fun setUpListeners() {
        binding.tvGithubLink.setOnClickListener {
            requireActivity().openBrowser(SensorViewModel.ARDUINO_SETUP_LINK)
        }
    }
}