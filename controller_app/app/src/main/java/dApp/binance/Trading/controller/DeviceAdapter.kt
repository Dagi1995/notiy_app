package dApp.binance.Trading.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceAdapter(
    private val devices: List<Device>,
    private val onItemClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size
}

class DeviceViewHolder(
    itemView: android.view.View,
    private val onItemClick: (Device) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(device: Device) {
        itemView.findViewById<TextView>(R.id.deviceName).text = device.name
        itemView.findViewById<TextView>(R.id.deviceStatus).text = "Status: ${device.status}"
        itemView.setOnClickListener { onItemClick(device) }
    }
}
