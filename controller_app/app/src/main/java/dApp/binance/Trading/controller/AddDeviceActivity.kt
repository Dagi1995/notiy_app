package dApp.binance.Trading.controller

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AddDeviceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        val deviceTokenInput = findViewById<EditText>(R.id.deviceTokenInput)
        val addButton = findViewById<Button>(R.id.addButton)
        val backButton = findViewById<Button>(R.id.backButton)

        addButton.setOnClickListener {
            val token = deviceTokenInput.text.toString().trim()
            if (token.isNotEmpty()) {
                // In a real app, you'd validate this token against the device
                Toast.makeText(this, "Device added (FCM token saved)", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        backButton.setOnClickListener { finish() }
    }
}
