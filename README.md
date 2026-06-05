# Carer USSD Control System

**A secure, consent-based system for remote USSD control on elderly care phones**

## Quick Overview

```
Your Phone (Controller App)
        ↓
   Firebase Cloud
        ↓
Grandma's Phone (Carer App) - Silent Background
```

Send USSD commands from your phone. Grandma's phone executes automatically. You get responses in real-time.

## Files Included

```
├── carer_app/              (Install on grandma's phone)
├── controller_app/         (Install on your phone)
├── controller_server/      (Optional backend)
└── DOCUMENTATION/          (Complete guides)
    ├── PROJECT_OVERVIEW.md (Start here)
    ├── QUICK_START.md      (5-minute setup)
    ├── SETUP_GUIDE.md      (Complete installation)
    ├── SECURITY.md         (Legal & consent)
    └── TROUBLESHOOTING.md  (Common issues)
```

## Quick Start (5 Minutes)

1. **Download APKs** from GitHub Actions
2. **Install Carer App** on grandma's phone
3. **Install Controller App** on your phone
4. **Open Controller App** → See "Grandma (Online)"
5. **Send USSD** → See response in 2-3 seconds

## Key Features

✅ Silent background execution (no notifications to grandma)  
✅ Multi-step USSD support (type number + send)  
✅ Real-time response capture  
✅ Firebase logging  
✅ Consent-based (requires written permission)  
✅ Audit trail  
✅ Easy to disable  

## Technology

- **Android Apps:** Kotlin + Material Design 3
- **Backend:** Node.js Express + Firebase
- **Database:** Firebase Realtime Database
- **Messaging:** Firebase Cloud Messaging
- **CI/CD:** GitHub Actions (auto-build APK)

## Installation Summary

```bash
# 1. Clone repository
git clone https://github.com/arfaneliyas1/carer-app.git
cd carer-app

# 2. Set up Firebase (see SETUP_GUIDE.md)
# - Create Firebase project
# - Add google-services.json files

# 3. Push to GitHub (optional, for auto-build)
git push origin main

# 4. Build APKs (GitHub Actions or local)
# - Download from Actions tab

# 5. Install on phones
# - Carer App on grandma's phone
# - Controller App on your phone

# 6. Start using!
# - Open controller app
# - Select device
# - Send USSD
```

## Documentation

| Guide | What | Time |
|-------|------|------|
| [QUICK_START.md](DOCUMENTATION/QUICK_START.md) | 5-minute setup | 5 min |
| [SETUP_GUIDE.md](DOCUMENTATION/SETUP_GUIDE.md) | Complete installation | 30 min |
| [PROJECT_OVERVIEW.md](DOCUMENTATION/PROJECT_OVERVIEW.md) | How it works | 10 min |
| [SECURITY.md](DOCUMENTATION/SECURITY.md) | Legal & consent | 20 min |
| [TROUBLESHOOTING.md](DOCUMENTATION/TROUBLESHOOTING.md) | Common issues | As needed |

## Security & Consent

⚠️ **CRITICAL:** This system requires **explicit written consent** from the person whose phone you're controlling.

Before deployment:
1. ✅ Get signed consent form
2. ✅ Explain what the system does
3. ✅ Allow them to disable anytime
4. ✅ Set up audit logging
5. ✅ Verify legal compliance

See [SECURITY.md](DOCUMENTATION/SECURITY.md) for complete requirements.

## System Architecture

```
┌────────────────────────────────────┐
│ You (Controller Phone)              │
│ ┌──────────────────────────────┐    │
│ │ Controller App               │    │
│ │ - List devices               │    │
│ │ - Send USSD: *127#          │    │
│ │ - View response: Balance... │    │
│ └──────────────────────────────┘    │
└────────────────────────────────────┘
              ↓
┌────────────────────────────────────┐
│ Firebase Cloud                      │
│ - FCM: Routes messages              │
│ - Database: Stores commands/logs    │
│ - Device Registry: Manages devices  │
└────────────────────────────────────┘
              ↓
┌────────────────────────────────────┐
│ Grandma (Carer Phone) - Silent      │
│ ┌──────────────────────────────┐    │
│ │ Carer App (Background)       │    │
│ │ 1. FCM: *127#               │    │
│ │ 2. Execute USSD             │    │
│ │ 3. Capture response         │    │
│ │ 4. Send to Firebase         │    │
│ │ No UI, no notifications     │    │
│ └──────────────────────────────┘    │
└────────────────────────────────────┘
              ↓
┌────────────────────────────────────┐
│ You See Response                    │
│ "Balance: $500" (Received 3s ago)   │
└────────────────────────────────────┘
```

## Feature Walkthrough

### Carer App (Grandma's Phone)

```
Open App
  ↓
✅ SERVICE ACTIVE
Device ID: arfu_001
Last Command: *127#
  ↓
[Settings] [View Logs]
```

- Shows status
- Device ID for pairing
- Last command executed
- View all logs
- Enable/Disable toggle

### Controller App (Your Phone)

```
Open App
  ↓
📱 Grandma (Online)
    Status: Ready
    Device ID: arfu_001
  ↓
[Tap Device]
  ↓
Enter USSD: [*127#]
[SEND]
  ↓
Response: Balance: $500
  ↓
[View History]
```

- Lists all devices
- Shows status
- Send USSD one tap
- Real-time responses
- Full history

## Common USSD Codes

**Ethiopia Telebirr:**
- `*127#` → Main menu
- `*127*1#` → Topup
- `*127*2#` → Transfer
- `*127*3#` → Balance

**Test Code:**
- `*123#` → Usually works to test

**Check your bank for their codes!**

## Troubleshooting

| Issue | Solution |
|-------|----------|
| App won't install | Enable "Unknown Sources" in Settings |
| Device not showing | Restart both apps, check internet |
| No response | Wait 5 seconds, check Accessibility enabled |
| USSD times out | Try different USSD code |

See [TROUBLESHOOTING.md](DOCUMENTATION/TROUBLESHOOTING.md) for more.

## Building from Source

### GitHub Actions (Recommended)

1. Push code to GitHub
2. Actions automatically builds APK
3. Download from Actions tab

### Local Build

```bash
# Carer App
cd carer_app
./gradlew assembleDebug

# Controller App
cd ../controller_app
./gradlew assembleDebug
```

Requirements: Java 17+, Android SDK 33, Gradle 8.0+

## Project Status

✅ **Complete & Ready to Deploy**

- ✅ Both Android apps fully functional
- ✅ Firebase integration working
- ✅ GitHub Actions CI/CD set up
- ✅ Complete documentation
- ✅ Security guidelines included
- ✅ Test cases provided

## Next Steps

1. **Read:** [PROJECT_OVERVIEW.md](DOCUMENTATION/PROJECT_OVERVIEW.md)
2. **Setup:** [SETUP_GUIDE.md](DOCUMENTATION/SETUP_GUIDE.md)
3. **Secure:** [SECURITY.md](DOCUMENTATION/SECURITY.md)
4. **Install:** APK on both phones
5. **Test:** Send first USSD
6. **Deploy:** Full production use

## Support

For issues:
1. Check [TROUBLESHOOTING.md](DOCUMENTATION/TROUBLESHOOTING.md)
2. Review [QUICK_START.md](DOCUMENTATION/QUICK_START.md)
3. Check Firebase Console
4. Verify permissions are correct

## License

This project is provided as-is for legitimate caregiving purposes.

**Misuse is prohibited and may result in legal consequences.**

## Important Disclaimer

This system is for **consensual elderly care** only.

- ✅ Must have written consent
- ✅ Must comply with local laws
- ✅ Must maintain audit logs
- ✅ Must protect data privacy

See [SECURITY.md](DOCUMENTATION/SECURITY.md) for complete legal requirements.

---

**Ready to get started?** → [QUICK_START.md](DOCUMENTATION/QUICK_START.md) ⏱️
