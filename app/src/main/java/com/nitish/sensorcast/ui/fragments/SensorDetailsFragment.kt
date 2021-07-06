package com.nitish.sensorcast.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.FragmentSensorDetailsBinding
import com.nitish.sensorcast.helpers.SensorDetails
import com.nitish.sensorcast.helpers.openBrowser
import com.nitish.sensorcast.helpers.round
import com.nitish.sensorcast.models.Status
import com.nitish.sensorcast.ui.SensorActivity
import com.nitish.sensorcast.ui.SensorViewModel

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
        "Resolution : ${sensorDetails.resolution} ${sensorDetails.units}".also {
            binding.tvSensorResolution.text = it
        }
        "Range : ${sensorDetails.range} ${sensorDetails.units}".also {
            binding.tvSensorRange.text = it
        }
        "Units : ${sensorDetails.units}".also { binding.tvSensorUnits.text = it }
    }

    private fun setUpObservers() {
        viewModel.isBtConnected.observe(viewLifecycleOwner, {
            binding.btnCastSensor.visibility =
                if (it == Status.CONNECTED) View.VISIBLE else View.GONE
        })

        viewModel.isCastEnabled.observe(viewLifecycleOwner, {
            if (it) {
                binding.btnCastSensor.apply {
                    ("Stop Casting").also { text = it }
                    backgroundTintList = resources.getColorStateList(R.color.red)
                    setTextColor(resources.getColor(R.color.white))
                }
            } else {
                binding.btnCastSensor.apply {
                    text = "Cast Sensor"
                    backgroundTintList = resources.getColorStateList(R.color.colorAccent)
                    setTextColor(resources.getColor(R.color.white))
                }
            }
        })

        viewModel.currentSensorValues.observe(viewLifecycleOwner, { values ->
            binding.textView12.visibility = View.VISIBLE
            binding.tvSensorValues.visibility = View.VISIBLE
            if (values.size == 1) {
                "${values[0].round(1)} ${sensorDetails.units}".also {
                    binding.tvSensorValues.text = it
                }
            } else if (values.size == 3) {
                ("x : ${values[0].round(1)} ${sensorDetails.units}\n" +
                        "y : ${values[1].round(1)} ${sensorDetails.units}\n" +
                        "z : ${values[2].round(1)} ${sensorDetails.units}\n").also {
                    binding.tvSensorValues.text = it
                }
            } else {
                binding.textView12.visibility = View.INVISIBLE
                binding.tvSensorValues.visibility = View.INVISIBLE
            }
        })
    }

    private fun setUpListeners() {
        binding.tvGithubLink.setOnClickListener {
            requireActivity().openBrowser(SensorViewModel.ARDUINO_SETUP_LINK)
        }

        binding.btnCastSensor.setOnClickListener {
            val isEnabled = viewModel.isCastEnabled.value
            viewModel.isCastEnabled.postValue(isEnabled?.not())
        }
    }
}