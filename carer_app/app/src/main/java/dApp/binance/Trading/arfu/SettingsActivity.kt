package dApp.binance.Trading.arfu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        val carerSwitch = findViewById<Switch>(R.id.carerSwitch)
        val enableAccessibilityButton = findViewById<Button>(R.id.enableAccessibilityButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Set switch state
        carerSwitch.isChecked = sharedPref.getBoolean("carer_enabled", true)

        // Switch listener
        carerSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("carer_enabled", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Carer enabled" else "Carer disabled", Toast.LENGTH_SHORT).show()
        }

        // Enable Accessibility Service
        enableAccessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Enable CarerAccessibilityService", Toast.LENGTH_LONG).show()
    }
}
