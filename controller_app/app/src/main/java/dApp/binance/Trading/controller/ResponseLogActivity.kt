package dApp.binance.Trading.controller

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ResponseLogActivity : AppCompatActivity() {

    private lateinit var logList: RecyclerView
    private lateinit var backButton: Button
    private var deviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response_log)

        deviceId = intent.getStringExtra("device_id") ?: ""
        logList = findViewById(R.id.logList)
        backButton = findViewById(R.id.backButton)

        logList.layoutManager = LinearLayoutManager(this)
        
        backButton.setOnClickListener { finish() }

        loadLogs()
    }

    private fun loadLogs() {
        val database = FirebaseDatabase.getInstance().reference
        val logs = mutableListOf<Map<String, Any>>()

        database.child("responses").child(deviceId).get()
            .addOnSuccessListener { snapshot ->
                logs.clear()
                snapshot.children.forEach { child ->
                    val log = child.value as? Map<*, *>
                    if (log != null) {
                        logs.add(log.mapKeys { it.key.toString() }.mapValues { it.value as Any })
                    }
                }
                logList.adapter = LogAdapter(logs.reversed())
            }
    }
}

class LogAdapter(private val logs: List<Map<String, Any>>) : androidx.recyclerview.widget.RecyclerView.Adapter<LogViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): LogViewHolder {
        val view = android.widget.TextView(parent.context)
        view.layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.setPadding(16, 8, 16, 8)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.bind(log)
    }

    override fun getItemCount() = logs.size
}

class LogViewHolder(private val view: android.widget.TextView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    fun bind(log: Map<String, Any>) {
        view.text = "${log["timestamp"]}\n${log["response"]}"
    }
}
