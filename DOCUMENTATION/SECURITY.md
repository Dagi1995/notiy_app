# Security, Consent & Legal Guide

## 🔴 Critical: Consent & Legal Compliance

### Legal Requirements

This app controls a person's phone remotely. **You MUST have explicit written consent.**

#### Required Before Deployment:

1. **Written Consent Document**
   ```
   I, [Grandma Name], consent to having the Carer App installed
   on my phone. I understand that:
   
   - This app allows [Your Name] to send USSD commands
   - Commands are executed automatically without my interaction
   - All commands are logged and can be audited
   - I can disable the app anytime by:
     - Uninstalling it, OR
     - Turning off the Carer toggle, OR
     - Disabling the Accessibility Service
   
   Signed: ________________  Date: ________________
   Witness: ________________  Date: ________________
   ```

2. **Grandma's Acknowledgment:**
   - She understands what the app does
   - She knows it controls USSD on her phone
   - She agrees willingly
   - She can disable it anytime

3. **Audit Logging:**
   - Keep records of all commands sent
   - Date, time, USSD code, response
   - In case of dispute or investigation

---

## 🔒 Technical Security

### Firebase Database Rules

Set restrictive rules so only authorized people can access data:

**In Firebase Console → Realtime Database → Rules:**

```json
{
  "rules": {
    "devices": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$device_id": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "logs": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$device_id": {
        ".read": "root.child('devices').child($device_id).child('owner').val() === auth.uid",
        ".write": "root.child('devices').child($device_id).child('owner').val() === auth.uid"
      }
    },
    "responses": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$device_id": {
        ".read": "root.child('devices').child($device_id).child('owner').val() === auth.uid",
        ".write": "root.child('devices').child($device_id).child('owner').val() === auth.uid"
      }
    },
    "commands": {
      ".write": "auth != null"
    }
  }
}
```

### Credential Security

- ✅ **google-services.json:** Keep private, don't commit to public repos
- ✅ **serviceAccountKey.json:** Never share, keep server-side only
- ✅ **Firebase Rules:** Use authentication tokens
- ✅ **Device IDs:** Change if compromised

### Network Security

- ✅ Use HTTPS for all API calls
- ✅ Validate Firebase token on each request
- ✅ Rate-limit API endpoints
- ✅ Never log sensitive data

---

## 📋 Audit Logging

### What Gets Logged

All commands and responses are automatically logged to Firebase:

```
logs/[device_id]/
├── timestamp: "2024-06-05 14:30:00"
├── ussd_code: "*127#"
├── status: "EXECUTED"
├── response: "Balance: $500"
└── device_id: "arfu_001"
```

### View Logs

**In Controller App:**
1. Tap device
2. Tap "View History"
3. See all commands sent

**In Firebase Console:**
1. Go to Realtime Database
2. Expand `logs/[device_id]`
3. See all entries with timestamps

### Audit Trail Best Practices

1. **Monthly Review:** Check logs for unusual activity
2. **Keep Records:** Export logs regularly
3. **Consent Check:** Verify grandma still consents
4. **Incident Log:** Document any concerns
5. **Retention Policy:** Keep logs for 1 year minimum

---

## ⚠️ Ethical Guidelines

### Dos ✅

- ✅ Ask permission before installing
- ✅ Explain what USSD codes will be used
- ✅ Show her how to disable the app
- ✅ Keep detailed audit logs
- ✅ Review logs regularly
- ✅ Respect her autonomy
- ✅ Use only for legitimate care purposes
- ✅ Inform her of security measures

### Don'ts ❌

- ❌ Install without explicit consent
- ❌ Hide the app or its purpose
- ❌ Send USSD codes she doesn't approve
- ❌ Access financial accounts without permission
- ❌ Share device access with others
- ❌ Disable her ability to disable the app
- ❌ Use for unauthorized transactions
- ❌ Sell or share logs with third parties

---

## 🛡️ Privacy Protection

### Data Stored in Firebase

| Data | Where | Retention | Access |
|------|-------|-----------|--------|
| Device ID | devices/ | Permanent | Controller + Carer app |
| Logs | logs/ | 1 year | Controller owner only |
| Responses | responses/ | 90 days | Controller owner only |
| FCM Tokens | devices/ | Until revoked | Firebase only |

### Data NOT Stored Locally

- ✅ Responses deleted after viewing
- ✅ Command history only in Firebase
- ✅ No local copy of sensitive data

### GDPR Compliance (if applicable)

- ✅ Consent documented
- ✅ Right to access: In Firebase Console
- ✅ Right to delete: Remove device from database
- ✅ Data portability: Export from Firebase
- ✅ Breach notification: Plan in place

---

## 🚨 Incident Response

### If App Is Misused

1. **Immediately disable:**
   - Uninstall from grandma's phone, OR
   - Turn off Carer toggle in app, OR
   - Disable Accessibility Service

2. **Audit logs:**
   - Check Firebase logs
   - Identify unauthorized commands
   - Document timestamps

3. **Notify authorities (if needed):**
   - If financial fraud occurred
   - If laws violated
   - With complete log records

4. **Review security:**
   - Change all credentials
   - Update Firebase rules
   - Add additional protections

---

## 📝 Pre-Deployment Checklist

Before installing on grandma's phone:

- [ ] Written consent signed and dated
- [ ] Grandma understands purpose
- [ ] Firebase Database Rules configured
- [ ] Credentials stored securely
- [ ] Audit logging enabled
- [ ] Backup of consent document
- [ ] Emergency disable plan documented
- [ ] Grandma knows how to disable app
- [ ] Monthly review schedule planned
- [ ] Incident response plan created

---

## ⚖️ Legal Disclaimer

**This system is provided for legitimate caregiving purposes only.**

The developer and user accept full responsibility for:
- Obtaining proper consent
- Complying with local laws
- Protecting data privacy
- Preventing unauthorized access
- Documenting audit trails

**Misuse of this system for fraud, unauthorized access, or illegal purposes is prohibited and may result in criminal charges.**

---

## Questions?

For security concerns:
1. Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
2. Check Firebase rules in console
3. Verify permissions are correct
4. Test with known USSD codes first

---

**Remember: Transparency and consent are the foundation of trust.** 🤝
