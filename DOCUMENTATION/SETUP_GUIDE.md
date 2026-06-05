# Complete Setup Guide

## Prerequisites

- ✅ 2 Android phones (API 23+, Android 6.0+)
- ✅ Google account (to use Firebase)
- ✅ WiFi or mobile internet on both phones
- ✅ Written consent from grandma

## Part 1: Project Setup

### Step 1.1: Clone/Download Code

```bash
git clone https://github.com/arfaneliyas1/carer-app.git
cd carer-app
```

### Step 1.2: Set Up Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **Create Project**
3. Name: "CarerApp"
4. Enable Google Analytics (optional)
5. Click **Create**

### Step 1.3: Add Android Apps to Firebase

**For Carer App:**
1. Click **Add App** → **Android**
2. Package name: `dApp.binance.Trading.arfu`
3. Download `google-services.json`
4. Place in: `carer_app/google-services.json`

**For Controller App:**
1. Click **Add App** → **Android**
2. Package name: `dApp.binance.Trading.controller`
3. Download `google-services.json`
4. Place in: `controller_app/google-services.json`

### Step 1.4: Enable Firebase Services

In Firebase Console:
1. **Realtime Database** → Click **Create Database**
   - Start in test mode
   - Region: closest to you
2. **Cloud Messaging** → Already enabled
3. **Authentication** → Enable (optional, for future security)

---

## Part 2: Build APKs

### Option A: Build on GitHub (Recommended)

1. **Push code to GitHub:**
   ```bash
   git add .
   git commit -m "Initial commit"
   git push origin main
   ```

2. **GitHub automatically builds:**
   - Go to **Actions** tab
   - Wait for build to complete (2-3 minutes)
   - Download APK artifact

### Option B: Build Locally

**Requirements:**
- Java 17+
- Android SDK API 33
- Gradle

**Build command:**
```bash
# For Carer App
cd carer_app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

# For Controller App
cd ../controller_app
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## Part 3: Install Carer App (Grandma's Phone)

### Step 3.1: Transfer APK

- **ADB:** `adb install carer-app-debug.apk`
- **File Transfer:** Email/USB the APK file
- **Direct:** Download from GitHub Actions

### Step 3.2: Install & Grant Permissions

1. **Open file manager** → Locate APK
2. **Tap APK** → **Install**
3. **Grant Permissions:**
   - Phone permission → **Allow** (for USSD)
   - Internet → **Allow** (for Firebase)
4. **Enable Accessibility Service:**
   - Tap "Enable Accessibility Service" button in app
   - Settings opens → Find "CarerAccessibilityService"
   - Toggle **ON**
   - Return to app
5. **Settings in App:**
   - Keep "Carer Enabled" toggle **ON**
   - Note the **Device ID** (e.g., "arfu_001")

### Step 3.3: Verify Installation

In the Carer App, you should see:
```
✅ SERVICE ACTIVE
Device ID: arfu_001
Last Command: None
```

---

## Part 4: Install Controller App (Your Phone)

### Step 4.1: Transfer APK

- **ADB:** `adb install controller-app-debug.apk`
- **File Transfer:** Email/USB the APK file

### Step 4.2: Install & Grant Permissions

1. **Open file manager** → Locate APK
2. **Tap APK** → **Install**
3. **Grant Internet permission** → **Allow**

### Step 4.3: First Launch

When you open the Controller App:
- It queries Firebase for registered devices
- You should see **"Grandma (arfu_001)"** listed as **Online**
- If not, wait 10 seconds and refresh

---

## Part 5: Send Your First USSD

### Step 5.1: Prepare Test USSD

Choose a USSD code:
- `*127#` - Telebirr (balance)
- `*123#` - Generic USSD
- Your bank's code

### Step 5.2: Send Command

1. **Open Controller App**
2. **Tap on "Grandma"** device
3. **Enter USSD code:** `*127#`
4. **Tap SEND**
5. **Wait 2-3 seconds**
6. You should see the response appear!

### Step 5.3: Verify

In **Carer App** on Grandma's phone:
- Last Command shows: `*127#`
- Check View Logs → Should show the execution

---

## Part 6: Multi-Step USSD (Optional)

### Example: Telebirr Transfer

**Step 1:** Send `*127#`
```
Response shows:
Select: 1=Topup 2=Transfer 3=Balance
```

**Step 2:** Send `2` (for Transfer)
```
Response shows:
Enter recipient:
```

**Step 3:** Send recipient details (if numeric)
```
...continue as needed
```

---

## Part 7: Optional - Run Controller Server

If you want REST API for testing:

```bash
# Navigate to server directory
cd controller_server

# Install dependencies
npm install

# Copy .env
cp .env.example .env

# Create serviceAccountKey.json
# (Download from Firebase Console → Service Accounts)

# Start server
npm start
# Server runs on http://localhost:3000
```

### Test API:

```bash
# Get devices
curl http://localhost:3000/devices

# Send USSD
curl -X POST http://localhost:3000/send-ussd \
  -H "Content-Type: application/json" \
  -d '{"device_id": "arfu_001", "ussd_code": "*127#"}'

# Get logs
curl http://localhost:3000/logs/arfu_001
```

---

## Troubleshooting Installation

| Problem | Solution |
|---------|----------|
| **APK won't install** | Enable "Unknown Sources" in Settings → Security |
| **Permission denied** | Tap "Allow" when prompted |
| **Device doesn't show in list** | Restart both apps, check internet |
| **Service status not active** | Restart Carer app, check permissions |
| **Accessibility not working** | Go to Settings → Accessibility → Find app → Toggle ON |
| **Firebase error** | Check google-services.json is correct |

---

## Security Setup

**Before going live:**
1. ✅ Have grandma sign consent form (see SECURITY.md)
2. ✅ Configure Firebase Database Rules (see SECURITY.md)
3. ✅ Test all USSD codes you'll use
4. ✅ Set up audit logging
5. ✅ Review SECURITY.md for full checklist

---

## Next Steps

- 📖 Read [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) for architecture
- 🔒 Read [SECURITY.md](SECURITY.md) for consent & compliance
- 🐛 Read [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for issues
- 📱 Read individual README files in each folder

---

**Setup Complete!** 🎉  
Your Carer USSD system is ready to use!
