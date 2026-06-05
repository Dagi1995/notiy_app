# CARER USSD Control System - Project Overview

## What is This?

A **remote elderly care system** that allows you to control USSD transactions on your grandma's phone from your own phone, without her needing to interact with it.

**Example Scenario:**
- Your grandma wants to check her bank balance
- You: Send `*127#` from your controller app
- Her phone: Automatically dials and captures the response
- You: See the balance in your controller app
- Grandma: Completely transparent, nothing visible on her phone

## 2-App Architecture

### **Carer App** (Grandma's phone)
- Silent background service
- Listens for USSD commands via Firebase Cloud Messaging (FCM)
- Automatically executes USSD codes
- Captures responses using Accessibility Service
- Sends all responses to Firebase database
- Zero UI notifications/interaction required from grandma

### **Controller App** (Your phone)
- Beautiful Material Design interface
- List of paired grandma phones
- Send USSD commands with one tap
- View real-time responses from grandma's phone
- Complete response history

### **Controller Server** (Node.js Backend)
- Routes FCM messages to correct devices
- Manages device registry
- Provides REST API for testing via curl

## Technology Stack

| Component | Technology |
|-----------|-----------|
| **Carer App** | Android (Kotlin), Firebase Cloud Messaging, Firebase Realtime Database, Accessibility Service |
| **Controller App** | Android (Kotlin), Firebase Realtime Database, Material Design 3 |
| **Server** | Node.js, Express, Firebase Admin SDK |
| **Database** | Firebase Realtime Database |
| **Messaging** | Firebase Cloud Messaging (FCM) |
| **CI/CD** | GitHub Actions (auto-build APK) |

## How It Works

```
┌──────────────────────────────────────────────────────────────┐
│ You (Controller Phone)                                        │
│ ┌────────────────────────────────────────────────────────┐   │
│ │ Controller App                                         │   │
│ │ - List devices                                         │   │
│ │ - Enter USSD code: *127#                              │   │
│ │ - Tap SEND                                             │   │
│ └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────┐
│ Firebase Cloud                                                │
│ ├─ FCM: Routes message                                        │
│ ├─ Realtime DB: Stores commands & responses                  │
│ └─ Device Registry: Knows all paired phones                   │
└──────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────┐
│ Grandma (Carer Phone) - Silent Background                    │
│ ┌────────────────────────────────────────────────────────┐   │
│ │ Carer App (Hidden)                                     │   │
│ │ 1. FCM message received: *127#                         │   │
│ │ 2. Execute USSD automatically                          │   │
│ │ 3. Accessibility Service captures: "Balance: $500"    │   │
│ │ 4. Send response to Firebase                           │   │
│ │ 5. No notifications, no UI shown                       │   │
│ └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
                           ↓
┌──────────────────────────────────────────────────────────────┐
│ You (Controller Phone) - Response Received                    │
│ ┌────────────────────────────────────────────────────────┐   │
│ │ Controller App                                         │   │
│ │ "Balance: $500" - (Received 2 seconds ago)             │   │
│ └────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

## Multi-Step USSD Support

The app handles interactive USSD dialogs (e.g., Telebirr):

**Step 1:** Send `*127#`
```
Popup: Select:
1 = Topup
2 = Transfer
3 = Balance
```

**Step 2:** You send `1`
```
Accessibility Service:
- Types "1" in input field
- Clicks "Send" button
```

**Step 3:** New dialog appears
```
Popup: Enter amount:
50 / 100 / 200
```

**Step 4:** You send `100`
```
...continues until transaction complete
```

## Features

✅ **Carer App:**
- Silent background execution
- FCM push notifications
- Accessibility Service for USSD response capture
- Multi-step USSD support (type number + click send)
- Firebase Realtime Database logging
- Auto-restart on device reboot
- Zero UI notifications

✅ **Controller App:**
- Device discovery (auto-lists registered phones)
- Send USSD commands
- View real-time responses
- Response history
- Material Design 3 UI

✅ **Server:**
- FCM message routing
- Device registry management
- REST API endpoints
- Logging

✅ **Development:**
- GitHub Actions auto-build APK
- Firebase integration ready
- Complete documentation
- Curl command examples

## Security & Consent

⚠️ **IMPORTANT:** This app must be used with **explicit consent** from the person whose phone you're controlling.

### Legal & Ethical Requirements:
1. ✅ Get written permission from grandma
2. ✅ Explain what the app does
3. ✅ Allow her to disable it anytime
4. ✅ Audit logging of all commands
5. ✅ Transparency about data storage

### Technical Security:
- ✅ Firebase authentication tokens
- ✅ Device IDs for isolation
- ✅ FCM device-specific routing
- ✅ Response captured locally (not stored on device)
- ✅ Firebase Realtime Database rules (restrict access)

## File Structure

```
carer-app-complete/
├── carer_app/              (Grandma's app - silent)
├── controller_app/         (Your app - controller)
├── controller_server/      (Backend - Node.js)
└── DOCUMENTATION/          (This folder)
```

## Quick Start

1. **Install Carer App on Grandma's Phone:**
   - Download APK from GitHub Actions
   - Install
   - Grant permissions (Phone, Internet, Accessibility)
   - App runs silently in background

2. **Install Controller App on Your Phone:**
   - Download APK from GitHub Actions
   - Install
   - Opens, auto-discovers registered devices

3. **Send USSD Command:**
   - Open Controller App
   - Tap device
   - Enter USSD code
   - Tap SEND
   - View response

4. **Optional: Run Controller Server:**
   ```bash
   npm install
   npm start
   ```

## Next Steps

- See [SETUP_GUIDE.md](SETUP_GUIDE.md) for detailed installation
- See [QUICK_START.md](QUICK_START.md) for 5-minute quick start
- See [SECURITY.md](SECURITY.md) for consent & security checklist
- See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues

---

**Build Date:** June 5, 2026  
**Status:** Ready for deployment
