# Controller App

**Remote USSD controller for elderly care**

## What It Does

Installed on your phone, this app lets you:
- See list of paired Carer App devices
- Send USSD commands to grandma's phone
- View real-time responses
- Check command history

## Key Features

✅ **Device Discovery** - Auto-finds registered Carer Apps  
✅ **Simple UI** - One-tap USSD sending  
✅ **Real-Time Responses** - Instant feedback  
✅ **Response History** - View past commands  
✅ **Material Design 3** - Modern, clean interface  

## Installation

1. Download APK from GitHub Actions
2. Open APK on phone
3. Grant internet permission
4. That's it!

## How to Use

1. **Open Controller App**
   - You'll see list of registered devices
   - Devices show "Online" or "Offline"

2. **Tap a Device**
   - Opens USSD send screen
   - Shows last response received

3. **Send USSD Code**
   - Type USSD code (e.g., `*127#`)
   - Tap "SEND"
   - Wait 2-3 seconds

4. **View Response**
   - Response appears in app
   - Tap "View History" to see past commands

## Example Workflow

```
1. Enter: *127#
   Response: Select 1=Topup 2=Transfer 3=Balance

2. Enter: 2 (for Transfer)
   Response: Enter recipient...

3. Enter: 0922123456 (recipient phone)
   Response: Enter amount...

4. Enter: 100 (amount)
   Response: Confirm transfer? Yes/No
```

## Troubleshooting

**No devices showing?**
- Carer App must be installed on grandma's phone first
- Restart both apps
- Check internet on both phones

**Response not appearing?**
- Wait longer (up to 10 seconds)
- Check internet connection
- Restart app

**App crashes?**
- Requires Android 6.0+ (API 23+)
- Check device compatibility

## Technical Details

- **API Level:** 23+ (Android 6.0+)
- **Package:** dApp.binance.Trading.controller
- **Database:** Firebase Realtime Database
- **UI:** Material Design 3 + RecyclerView

## Pair New Device

To add another Carer App:
1. Tap "+ Add Device" button
2. Enter FCM token from Carer App
3. Device appears in list

## For More Information

See [PROJECT_OVERVIEW.md](../DOCUMENTATION/PROJECT_OVERVIEW.md) in documentation folder.
