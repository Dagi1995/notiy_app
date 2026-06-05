# Quick Start Guide (5 Minutes)

## Prerequisites
- 2 Android phones (Grandma's + Yours)
- Google account (for Firebase)
- Internet connection on both phones

## Step 1: Get the APKs (2 minutes)

1. Go to GitHub: https://github.com/arfaneliyas1/carer-app
2. Click **Actions**
3. Click the latest successful build
4. Download **carer-app-debug.apk** (for grandma)
5. Download **controller-app-debug.apk** (for you)

**Or** Build locally if you have Android SDK installed

## Step 2: Install Carer App on Grandma's Phone (1 minute)

1. Transfer `carer-app-debug.apk` to grandma's phone
2. Open file manager → Tap APK file
3. **Tap Install**
4. **Grant permissions when prompted:**
   - "Allow Phone calls?" → **Allow**
   - "Allow Internet?" → **Allow**  
   - "Enable Accessibility Service?" → **Go to Settings** → Toggle **ON**
5. Done! App runs silently

## Step 3: Install Controller App on Your Phone (1 minute)

1. Transfer `controller-app-debug.apk` to your phone
2. Open file manager → Tap APK file
3. **Tap Install**
4. **Grant permissions:** Internet only
5. Done!

## Step 4: Send First USSD (1 minute)

1. **Open Controller App** on your phone
2. You should see **"Grandma (Online)"** in the device list
3. **Tap the device**
4. **Enter USSD code:** `*127#` (example)
5. **Tap SEND**
6. **Wait 2-3 seconds** → You'll see the response!

---

## Example USSD Codes

- **Telebirr:** `*127#` → Balance, Transfer, Topup
- **Bank Balance:** Varies by bank
- **SMS:** `*123#` → Might show SMS balance
- **Mobile Data:** `*131#` → Data balance

## Troubleshooting

| Problem | Solution |
|---------|----------|
| No device shows up | Restart both apps, check internet |
| Can't see response | Wait 5 seconds, check Accessibility Service is enabled |
| USSD times out | Check that app has phone permission |
| App crashes | Check Android version 6.0+ |

---

**That's it! You're done!** 🎉

For detailed setup → [SETUP_GUIDE.md](SETUP_GUIDE.md)  
For common issues → [TROUBLESHOOTING.md](TROUBLESHOOTING.md)  
For security → [SECURITY.md](SECURITY.md)
