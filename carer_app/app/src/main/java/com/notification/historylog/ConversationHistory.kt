package com.notification.historylog

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class ConversationMessage(
    val sender: String, // "user", "ussd", "error"
    val content: String,
    val timestamp: Long
)

data class Conversation(
    val ussdCode: String,
    val startTime: Long,
    val messages: MutableList<ConversationMessage> = mutableListOf(),
    var endTime: Long? = null,
    var status: String = "active" // active, completed, failed
)

object ConversationHistory {

    private var currentConversation: Conversation? = null
    private val MAX_CONVERSATIONS = 50

    /**
     * Start a new USSD conversation
     */
    fun startConversation(ussdCode: String, context: Context) {
        currentConversation = Conversation(
            ussdCode = ussdCode,
            startTime = System.currentTimeMillis()
        )
        Log.d("ConversationHistory", "Started new conversation: $ussdCode")
    }

    /**
     * Add a message to current conversation
     */
    fun addMessage(sender: String, content: String) {
        currentConversation?.let {
            it.messages.add(
                ConversationMessage(
                    sender = sender,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
            )
            Log.d("ConversationHistory", "Added message from $sender: $content")
        }
    }

    /**
     * End current conversation and save to Firebase
     */
    fun endConversation(context: Context, status: String = "completed", hasInputField: Boolean = false) {
        currentConversation?.let { conversation ->
            conversation.endTime = System.currentTimeMillis()
            conversation.status = if (!hasInputField) "completed" else "active"
            
            // Save to Firebase
            saveConversationToFirebase(conversation, context)
            
            // Also save locally for history
            saveConversationLocally(conversation, context)
            
            Log.d("ConversationHistory", "Ended conversation with status: ${conversation.status}")
        }
    }

    /**
     * Save conversation to Firebase
     */
    private fun saveConversationToFirebase(conversation: Conversation, context: Context) {
        try {
            val database = FirebaseDatabase.getInstance().reference
            val deviceId = FirebaseHelper.getDeviceId(context)
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val startTime = timeFormat.format(Date(conversation.startTime))

            val messagesJson = JSONArray()
            conversation.messages.forEach { msg ->
                messagesJson.put(JSONObject().apply {
                    put("sender", msg.sender)
                    put("content", msg.content)
                    put("timestamp", SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(msg.timestamp)))
                })
            }

            val conversationData = mapOf(
                "ussd_code" to conversation.ussdCode,
                "start_time" to startTime,
                "duration_ms" to (conversation.endTime?.minus(conversation.startTime) ?: 0),
                "status" to conversation.status,
                "message_count" to conversation.messages.size,
                "device_id" to deviceId
            )

            database.child("conversation_history").child(deviceId).push().setValue(conversationData)
                .addOnFailureListener { e ->
                    Log.e("ConversationHistory", "Failed to save: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("ConversationHistory", "Error saving conversation: ${e.message}")
        }
    }

    /**
     * Save conversation locally in SharedPreferences (with size limit)
     */
    private fun saveConversationLocally(conversation: Conversation, context: Context) {
        try {
            val sharedPref = context.getSharedPreferences("ConversationHistory", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            // Get existing conversations
            val allConversations = mutableListOf<String>()
            val keys = sharedPref.all.keys.filter { it.startsWith("conversation_") }
            keys.forEach { allConversations.add(it) }

            // Remove oldest if we exceed limit
            if (allConversations.size >= MAX_CONVERSATIONS) {
                val oldestKey = allConversations.minByOrNull { 
                    sharedPref.getLong(it.replace("conversation_", "end_time_"), 0)
                }
                if (oldestKey != null) {
                    editor.remove(oldestKey)
                }
            }

            // Save new conversation
            val conversationKey = "conversation_${conversation.startTime}"
            val conversationJson = JSONObject().apply {
                put("ussd_code", conversation.ussdCode)
                put("start_time", conversation.startTime)
                put("end_time", conversation.endTime)
                put("status", conversation.status)
                put("duration_ms", (conversation.endTime?.minus(conversation.startTime) ?: 0))
                put("message_count", conversation.messages.size)
            }

            editor.putString(conversationKey, conversationJson.toString())
            editor.apply()

            Log.d("ConversationHistory", "Saved conversation locally")
        } catch (e: Exception) {
            Log.e("ConversationHistory", "Error saving locally: ${e.message}")
        }
    }

    /**
     * Get all conversations
     */
    fun getAllConversations(context: Context): List<Conversation> {
        try {
            val sharedPref = context.getSharedPreferences("ConversationHistory", Context.MODE_PRIVATE)
            val conversations = mutableListOf<Conversation>()

            sharedPref.all.forEach { (key, value) ->
                if (key.startsWith("conversation_") && value is String) {
                    try {
                        val json = JSONObject(value)
                        conversations.add(
                            Conversation(
                                ussdCode = json.getString("ussd_code"),
                                startTime = json.getLong("start_time"),
                                endTime = json.getLong("end_time"),
                                status = json.getString("status")
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("ConversationHistory", "Error parsing conversation: ${e.message}")
                    }
                }
            }

            return conversations.sortedByDescending { it.startTime }
        } catch (e: Exception) {
            Log.e("ConversationHistory", "Error getting conversations: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Clear all conversations
     */
    fun clearAllConversations(context: Context) {
        try {
            val sharedPref = context.getSharedPreferences("ConversationHistory", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            currentConversation = null
            Log.d("ConversationHistory", "Cleared all conversations")
        } catch (e: Exception) {
            Log.e("ConversationHistory", "Error clearing: ${e.message}")
        }
    }
}
