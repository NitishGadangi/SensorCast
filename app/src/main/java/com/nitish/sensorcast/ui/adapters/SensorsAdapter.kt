package com.nitish.sensorcast.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nitish.sensorcast.databinding.ItemSensorOverviewBinding
import com.nitish.sensorcast.helpers.SensorDetails

class SensorsAdapter : RecyclerView.Adapter<SensorsAdapter.SensorsAdapterViewHolder>() {

    inner class SensorsAdapterViewHolder(val binding: ItemSensorOverviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<SensorDetails>() {
        override fun areItemsTheSame(oldItem: SensorDetails, newItem: SensorDetails): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: SensorDetails, newItem: SensorDetails): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorsAdapterViewHolder {
        val binding = ItemSensorOverviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SensorsAdapterViewHolder(binding)
    }

    private var onItemClickListener: ((SensorDetails) -> Unit)? = null

    override fun onBindViewHolder(holder: SensorsAdapterViewHolder, position: Int) {
        val sensorDetails = differ.currentList[position]
        holder.binding.apply {
            tvSensorName.text = sensorDetails.name
            "Vendor : ${sensorDetails.vendor}".also { tvManufacturerName.text = it }
            "Resolution : ${sensorDetails.resolution} ${sensorDetails.units}".also { tvResolution.text = it }
            "Range : ${sensorDetails.range} ${sensorDetails.units}".also { tvMaximumRange.text = it }

            root.setOnClickListener {
                onItemClickListener?.let {
                    it(sensorDetails)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener: (SensorDetails) -> Unit) {
        onItemClickListener = listener
    }

}