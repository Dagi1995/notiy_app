package com.notification.controller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var deviceList: RecyclerView
    private lateinit var addDeviceButton: Button
    private val devices = mutableListOf<Device>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceList = findViewById(R.id.deviceList)
        addDeviceButton = findViewById(R.id.addDeviceButton)

        deviceList.layoutManager = LinearLayoutManager(this)
        
        addDeviceButton.setOnClickListener {
            startActivity(Intent(this, AddDeviceActivity::class.java))
        }

        loadDevices()
    }

    override fun onResume() {
        super.onResume()
        loadDevices()
    }

    private fun loadDevices() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("devices").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                devices.clear()
                snapshot.children.forEach { child ->
                    val data = child.value as? Map<*, *>
                    if (data != null && data["app_type"] == "carer") {
                        val lastSeen = data["last_seen"] as? Long ?: 0L
                        val currentTime = System.currentTimeMillis()
                        val isOnline = (currentTime - lastSeen) < (10 * 60 * 1000)

                        devices.add(Device(
                            id = child.key ?: "",
                            deviceId = data["device_id"] as? String ?: "Unknown",
                            modelName = data["model_name"] as? String ?: "Generic Android",
                            customName = data["custom_name"] as? String ?: "",
                            status = if (isOnline) "online" else "offline",
                            fcmToken = data["fcm_token"] as? String ?: "",
                            batteryLevel = (data["battery_level"] as? Long)?.toInt() ?: -1,
                            accessibilityActive = data["accessibility_active"] as? Boolean ?: false,
                            simCount = (data["sim_count"] as? Long)?.toInt() ?: 1
                        ))
                    }
                }

                deviceList.adapter = DeviceAdapter(devices) { device ->
                    // Instead of going straight to USSD, go to the new Dashboard
                    val intent = Intent(this@MainActivity, DeviceDashboardActivity::class.java)
                    intent.putExtra("device_id", device.id)
                    intent.putExtra("device_name", if (device.customName.isNotEmpty()) device.customName else device.modelName)
                    intent.putExtra("fcm_token", device.fcmToken)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

data class Device(
    val id: String,
    val deviceId: String,
    val modelName: String,
    val customName: String,
    val status: String,
    val fcmToken: String,
    val batteryLevel: Int = -1,
    val accessibilityActive: Boolean = false,
    val simCount: Int = 1
)
