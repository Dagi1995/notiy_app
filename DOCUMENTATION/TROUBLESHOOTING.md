# Troubleshooting Guide

## Installation Issues

### APK Won't Install

**Problem:** "App not installed" error when trying to install APK

**Solutions:**
1. **Enable Unknown Sources:**
   - Settings → Security → Unknown Sources → Toggle ON
2. **Check Android Version:**
   - Needs Android 6.0+ (API 23+)
   - Check: Settings → About → Android Version
3. **Free Up Storage:**
   - Delete unused apps/files
   - Need ~100MB free
4. **Try Different Installation Method:**
   - Via USB: `adb install app.apk`
   - Via file manager: Find APK, tap it
   - Via email attachment

---

## Permission Issues

### "Permission Denied" on Launch

**Problem:** App crashes when opening

**Solution:**
1. **Open Settings** → Apps → [App Name] → Permissions
2. **Enable:**
   - ✅ Phone (CALL_PHONE)
   - ✅ Internet
   - ✅ Accessibility Service (carer app only)
3. **Restart app**

### Accessibility Service Not Working

**Problem:** USSD responses not being captured

**Solutions:**
1. **Check if enabled:**
   - Settings → Accessibility → Find "CarerAccessibilityService"
   - Should be **ON** (toggle is green)

2. **Re-enable if stuck:**
   - Settings → Accessibility → Toggle **OFF**
   - Wait 10 seconds
   - Toggle back **ON**

3. **Restart device:**
   - Sometimes Android caches accessibility state

---

## Connectivity Issues

### Device Doesn't Show in Controller App

**Problem:** "No devices found" when opening controller app

**Diagnosis:**
1. Is Carer App installed on grandma's phone? ✓
2. Is Carer App service running? ✓
3. Is Internet working on both phones? ✓
4. Are both phones on same WiFi/network? (Not required, but helps)

**Solutions:**
1. **Restart both apps:**
   - Close completely (swipe away)
   - Open again
   - Wait 10 seconds

2. **Check Firebase Console:**
   - Go to: https://console.firebase.google.com
   - Realtime Database
   - Expand `devices/`
   - You should see `arfu_001` (or device ID)

3. **Check Internet:**
   - Both phones need active internet
   - WiFi or mobile data works
   - Not blocked by firewall

4. **Restart WiFi:**
   - Turn WiFi OFF for 10 seconds
   - Turn back ON
   - Reconnect both phones

---

## USSD Execution Issues

### USSD Code Not Executing

**Problem:** You tap SEND but nothing happens

**Checklist:**
1. ✅ Device shows "Online"?
2. ✅ USSD code is correct?
3. ✅ Internet on grandma's phone?
4. ✅ Phone permission granted?
5. ✅ Airplane mode OFF?

**Solutions:**
1. **Check phone permission:**
   - Settings → Apps → CarerApp → Permissions
   - CALL_PHONE should be **Allowed**

2. **Disable Airplane Mode:**
   - Swipe down from top
   - Tap Airplane icon (should be OFF)

3. **Try simple USSD:**
   - Instead of `*127#`, try `*123#`
   - Some USSD codes may not work on the network

4. **Wait longer:**
   - USSD can take 5-10 seconds
   - Don't close app while waiting

---

## Response Issues

### No Response Received

**Problem:** You send USSD but response never appears

**Why this happens:**
1. USSD code invalid for network
2. Grandma's phone lost connection
3. Accessibility Service didn't capture response
4. Firebase database issue

**Solutions:**
1. **Check service is running:**
   - On grandma's phone, open Carer App
   - Should show "✅ SERVICE ACTIVE"
   - If not, restart app

2. **Check Accessibility Service:**
   - Settings → Accessibility → Verify "CarerAccessibilityService" is ON

3. **Test with known USSD:**
   - Try `*123#` (usually works everywhere)
   - Or ask grandma what USSD codes work on her phone

4. **Check Firebase:**
   - Go to Firebase Console
   - Realtime Database
   - Expand `responses/arfu_001/`
   - Does the response exist there?

5. **If response in Firebase but not in app:**
   - Close and reopen controller app
   - Refresh by going back and tapping device again

---

## Firebase Issues

### Firebase Error in App

**Problem:** Error message mentions Firebase

**Solutions:**
1. **Check google-services.json:**
   - Carer App: `carer_app/google-services.json` should exist
   - Controller App: `controller_app/google-services.json` should exist
   - Both files should NOT be empty

2. **Check Firebase Project:**
   - https://console.firebase.google.com
   - Project should exist
   - Realtime Database should be running

3. **Verify Realtime Database:**
   - Firebase Console → Realtime Database
   - Should show "Running" status
   - If not, click **Create Database**

4. **Check Database URL:**
   - In google-services.json, look for `"database_url"`
   - Should look like: `"https://arfu27-c34b3-default-rtdb.firebaseio.com"`

---

## Server Issues (if running locally)

### Server Won't Start

**Problem:** Error when running `npm start`

**Solutions:**
1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Create serviceAccountKey.json:**
   - Download from Firebase Console
   - Service Accounts tab
   - Save as `serviceAccountKey.json` in server folder

3. **Check .env file:**
   ```bash
   cp .env.example .env
   ```

4. **Node version:**
   - Need Node 14+ installed
   - Check: `node --version`

### Server Starts but Apps Can't Connect

**Problem:** Apps running locally, server on same machine

**Solutions:**
1. **Use local IP address:**
   - Find your computer's IP: `ipconfig` (Windows) or `ifconfig` (Mac)
   - Instead of `localhost:3000`, use `192.168.x.x:3000`
   - Update server URL in apps

2. **Firewall:**
   - Windows Defender/Firewall may block port 3000
   - Allow Node.js through firewall

3. **CORS:**
   - Apps on different IP? CORS might block requests
   - Uncomment CORS in server: `app.use(cors())`

---

## Performance Issues

### App Freezes/Lags

**Problem:** Controller App is slow to respond

**Solutions:**
1. **Clear app cache:**
   - Settings → Apps → [App Name] → Storage → Clear Cache

2. **Close background apps:**
   - Close other apps using lots of memory
   - Restart phone if very slow

3. **Check internet speed:**
   - If very slow WiFi, may delay Firebase queries

4. **Check Firebase:**
   - If database has millions of logs, queries slow
   - Clear old logs from Firebase

---

## USSD Code Reference

### Common Codes by Provider

**Telebirr (Ethiopia):**
- `*127#` → Main menu
- `*127*1#` → Topup
- `*127*2#` → Transfer
- `*127*3#` → Balance

**Banks (Ethiopia):**
- CBE: `*901#`
- Dashen: `*973#`
- NIB: `*227#`

**Generic:**
- `*123#` → Usually shows something
- `*131#` → Often data balance
- `*121#` → Often account info

**Test these with grandma first!**

---

## Still Having Issues?

### Collect Information

When asking for help, provide:
1. **Device info:** Model, Android version
2. **Error message:** Exact text from app
3. **What you tried:** List all solutions you tried
4. **Firebase status:** Runs or error?
5. **Logs:** From Firebase Console (if applicable)

### Check Logs

**In Carer App:**
1. Tap "View Logs"
2. Take screenshot
3. Share errors

**In Firebase Console:**
1. Go to Realtime Database
2. Expand `logs/arfu_001/`
3. See what was logged

---

## Quick Restart Procedure

If nothing works, try this:

1. **On Grandma's Phone:**
   - Open Carer App
   - Tap Settings
   - Toggle OFF "Carer Enabled"
   - Wait 5 seconds
   - Toggle back ON
   - Close and reopen app

2. **On Your Phone:**
   - Close Controller App completely
   - Wait 10 seconds
   - Open again

3. **Restart WiFi (both phones):**
   - Turn WiFi OFF
   - Wait 10 seconds
   - Turn back ON

This fixes 80% of issues! ✅

---

**Still stuck?** Review [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) for architecture overview and see if you can spot the issue.
