package dApp.binance.Trading.arfu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object UssdHelper {

    /**
     * Executes a USSD code.
     * We use ACTION_CALL with URI encoding to force the system dialer to appear.
     */
    fun executeUssd(context: Context, code: String) {
        Log.d("UssdHelper", "Starting execution for code: $code")
        
        try {
            // The key for USSD via Intent is to encode the '#' as %23
            // and use ACTION_CALL.
            val encodedCode = Uri.encode(code)
            val ussdUri = Uri.parse("tel:$encodedCode")
            
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = ussdUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            Log.d("UssdHelper", "Launching Dialer Intent: tel:$encodedCode")
            context.startActivity(intent)
            
        } catch (e: Exception) {
            Log.e("UssdHelper", "FAILED to launch dialer: ${e.message}")
            
            // Fallback for some devices: try dialer without auto-calling
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(code)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                Log.d("UssdHelper", "Fallback to ACTION_DIAL successful")
            } catch (e2: Exception) {
                Log.e("UssdHelper", "Ultimate failure: ${e2.message}")
            }
        }
    }
}
