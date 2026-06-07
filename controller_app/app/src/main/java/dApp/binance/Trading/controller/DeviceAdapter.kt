package dApp.binance.Trading.controller

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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
    itemView: View,
    private val onItemClick: (Device) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    fun bind(device: Device) {
        val displayName = if (device.customName.isNotEmpty()) device.customName else device.modelName
        itemView.findViewById<TextView>(R.id.deviceName).text = displayName
        
        val statusText = itemView.findViewById<TextView>(R.id.deviceStatus)
        statusText.text = if (device.status == "online") "● Online" else "● Offline"
        statusText.setTextColor(if (device.status == "online") Color.GREEN else Color.GRAY)

        val infoText = itemView.findViewById<TextView>(R.id.deviceInfo)
        infoText.text = "Model: ${device.modelName} | Battery: ${if (device.batteryLevel != -1) "${device.batteryLevel}%" else "--%"}"

        itemView.setOnClickListener { onItemClick(device) }
    }
}
