package com.notification.historylog

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
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

    fun registerDevice(context: Context, fcmToken: String, simCount: Int = -1) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = getDeviceId(context)
            
            var actualSimCount = simCount
            if (actualSimCount == -1) {
                val sharedPref = context.getSharedPreferences("CarerSettings", Context.MODE_PRIVATE)
                actualSimCount = sharedPref.getInt("sim_count", 1)
            }

            // Get device model
            val modelName = "${Build.MANUFACTURER} ${Build.MODEL}"

            val deviceData = mutableMapOf<String, Any>(
                "device_id" to deviceId,
                "model_name" to modelName,
                "fcm_token" to fcmToken,
                "app_type" to "carer",
                "sim_count" to actualSimCount,
                "registered_at" to System.currentTimeMillis()
            )

            database.child("devices").child(deviceId).updateChildren(deviceData)
            updateDeviceStatus(context)
            
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error registering device: ${e.message}")
        }
    }

    fun updateDeviceStatus(context: Context) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = getDeviceId(context)

            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1

            val isAccessibilityEnabled = CarerAccessibilityService.isAccessibilityServiceEnabled(context)

            val statusData = mapOf(
                "battery_level" to batteryPct,
                "accessibility_active" to isAccessibilityEnabled,
                "last_seen" to System.currentTimeMillis(),
                "status" to "online"
            )

            database.child("devices").child(deviceId).updateChildren(statusData)
            Log.d("FirebaseHelper", "Heartbeat sent: Battery $batteryPct%, Acc: $isAccessibilityEnabled")
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error updating status: ${e.message}")
        }
    }
}
