package com.nitish.sensorcast.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nitish.sensorcast.R
import com.nitish.sensorcast.databinding.FragmentSensorsOverviewBinding
import com.nitish.sensorcast.ui.adapters.SensorsAdapter

class SensorsOverviewFragment : Fragment(R.layout.fragment_sensors_overview) {

    private val viewModel by lazy {
        (activity as SensorActivity).viewModel
    }

    lateinit var binding: FragmentSensorsOverviewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSensorsOverviewBinding.bind(view)

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val sensorsAdapter = SensorsAdapter()
        binding.rvSensorDetails.apply {
            adapter = sensorsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        sensorsAdapter.setOnItemClickListener {
            val directions =
                SensorsOverviewFragmentDirections.actionSensorsOverviewFragmentToSensorDetailsFragment(
                    it
                )
            findNavController().navigate(directions)
        }
        sensorsAdapter.differ.submitList(viewModel.getSensorsList())
    }
}