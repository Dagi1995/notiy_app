package com.notification.historylog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.Log

object UssdHelper {

    fun executeUssd(context: Context, code: String, simSlot: Int = 0) {
        Log.d("UssdHelper", "Executing USSD: $code on SIM slot: $simSlot")

        try {
            val intent = Intent(Intent.ACTION_CALL)
            val ussdUri = Uri.fromParts("tel", code, "#")
            intent.data = ussdUri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Handle SIM selection
            if (simSlot >= 0) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                
                try {
                    val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                    if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size > simSlot) {
                        val subInfo = activeSubscriptionInfoList[simSlot]
                        val subId = subInfo.subscriptionId
                        
                        // There are multiple ways to set SIM slot, we'll try the most common ones
                        intent.putExtra("com.android.phone.extra.slot", simSlot)
                        intent.putExtra("simSlot", simSlot)
                        
                        // For modern Android (TelecomManager)
                        val phoneAccounts = telecomManager.callCapablePhoneAccounts
                        for (accountHandle in phoneAccounts) {
                            val account = telecomManager.getPhoneAccount(accountHandle)
                            if (account.label.toString().contains("SIM ${simSlot + 1}", ignoreCase = true) ||
                                account.shortDescription.toString().contains("SIM ${simSlot + 1}", ignoreCase = true)) {
                                intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
                                break
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("UssdHelper", "Permission denied for SIM detection")
                }
            }

            context.startActivity(intent)
            Log.d("UssdHelper", "USSD Intent launched")
        } catch (e: Exception) {
            Log.e("UssdHelper", "Error launching USSD Intent: ${e.message}")
        }
    }
}
