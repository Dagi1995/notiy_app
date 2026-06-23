package com.notification.historylog

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseLogHelper {

    fun logCommand(
        ussdCode: String,
        status: String,
        context: Context,
        error: String = ""
    ) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = FirebaseHelper.getDeviceId(context)
            val timestamp = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = timeFormat.format(Date(timestamp))

            val logData = mapOf(
                "ussd_code" to ussdCode,
                "status" to status,
                "timestamp" to dateString,
                "time_ms" to timestamp,
                "error" to error,
                "device_id" to deviceId
            )

            database.child("logs").child(deviceId).push().setValue(logData)
                .addOnFailureListener { e ->
                    Log.e("FirebaseLogHelper", "Failed to log command: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseLogHelper", "Error logging command: ${e.message}")
        }
    }

    fun logResponse(response: String, context: Context, hasInputField: Boolean = true) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = FirebaseHelper.getDeviceId(context)
            val timestamp = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = timeFormat.format(Date(timestamp))

            val logData = mapOf(
                "response" to response,
                "status" to "success",
                "has_input_field" to hasInputField,
                "timestamp" to dateString,
                "time_ms" to timestamp,
                "device_id" to deviceId
            )

            database.child("responses").child(deviceId).push().setValue(logData)
                .addOnFailureListener { e ->
                    Log.e("FirebaseLogHelper", "Failed to log response: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseLogHelper", "Error logging response: ${e.message}")
        }
    }

    fun getLogs(context: Context, callback: (List<Map<String, String>>) -> Unit) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = FirebaseHelper.getDeviceId(context)

            database.child("logs").child(deviceId).get()
                .addOnSuccessListener { snapshot ->
                    val logs = mutableListOf<Map<String, String>>()
                    snapshot.children.forEach { child ->
                        val log = child.value as? Map<*, *>
                        if (log != null) {
                            logs.add(log.mapKeys { it.key.toString() }.mapValues { it.value.toString() })
                        }
                    }
                    callback(logs.reversed())
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseLogHelper", "Failed to get logs: ${e.message}")
                    callback(emptyList())
                }
        } catch (e: Exception) {
            Log.e("FirebaseLogHelper", "Error getting logs: ${e.message}")
            callback(emptyList())
        }
    }
}
