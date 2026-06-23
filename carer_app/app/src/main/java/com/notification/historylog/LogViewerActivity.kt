package com.notification.historylog

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LogViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)

        val logText = findViewById<TextView>(R.id.logText)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val backButton = findViewById<Button>(R.id.backButton)

        refreshButton.setOnClickListener {
            refreshLogs(logText)
        }

        backButton.setOnClickListener {
            finish()
        }

        refreshLogs(logText)
    }

    private fun refreshLogs(logText: TextView) {
        FirebaseLogHelper.getLogs(this) { logs ->
            runOnUiThread {
                logText.text = logs.joinToString("\n\n") { log ->
                    "${log["timestamp"]}\n${log["ussd_code"]}\nStatus: ${log["status"]}"
                }
            }
        }
    }
}
