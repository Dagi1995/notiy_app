package dApp.binance.Trading.arfu

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.installations.FirebaseInstallations
import java.util.UUID

object FirebaseHelper {

    fun getDeviceId(context: Context): String {
        val sharedPref = context.getSharedPreferences("CarerSettings", Context.MODE_PRIVATE)
        var deviceId = sharedPref.getString("device_id", null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().substring(0, 8)
            sharedPref.edit().putString("device_id", deviceId).apply()
        }

        return deviceId
    }

    fun registerDevice(context: Context, fcmToken: String) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = getDeviceId(context)

            val deviceData = mapOf(
                "device_id" to deviceId,
                "fcm_token" to fcmToken,
                "status" to "active",
                "registered_at" to System.currentTimeMillis(),
                "app_type" to "carer"
            )

            database.child("devices").child(deviceId).setValue(deviceData)
                .addOnSuccessListener {
                    Log.d("FirebaseHelper", "Device registered: $deviceId")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseHelper", "Failed to register device: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error registering device: ${e.message}")
        }
    }
}
