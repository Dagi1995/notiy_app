# Controller Server

**Node.js backend for USSD command routing**

## What It Does

Optional backend server that:
- Routes FCM messages to correct devices
- Manages device registry
- Provides REST API for testing
- Stores logs and responses

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Firebase

Download `serviceAccountKey.json` from Firebase Console:
1. Go to Firebase Console
2. Project Settings → Service Accounts
3. Click "Generate New Private Key"
4. Save as `serviceAccountKey.json` in server folder

### 3. Configure Environment

```bash
cp .env.example .env
# Edit .env if needed
```

### 4. Start Server

```bash
npm start
# Server runs on http://localhost:3000
```

## API Endpoints

### Health Check

```bash
GET /health
```

Response:
```json
{ "status": "OK" }
```

### Get All Devices

```bash
GET /devices
```

Response:
```json
{
  "success": true,
  "devices": [
    {
      "id": "arfu_001",
      "device_id": "arfu_001",
      "fcm_token": "...",
      "status": "active"
    }
  ]
}
```

### Send USSD Command

```bash
POST /send-ussd

{
  "device_id": "arfu_001",
  "ussd_code": "*127#",
  "auto_execute": false
}
```

Response:
```json
{
  "success": true,
  "message_id": "..."
}
```

### Get Logs for Device

```bash
GET /logs/arfu_001
```

### Get Responses for Device

```bash
GET /responses/arfu_001
```

## Curl Examples

See `CURL_EXAMPLES.sh` for ready-to-copy curl commands:

```bash
# Send USSD
curl -X POST http://localhost:3000/send-ussd \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "arfu_001",
    "ussd_code": "*127#"
  }'

# Get logs
curl http://localhost:3000/logs/arfu_001
```

## Deployment

### Heroku

```bash
# Install Heroku CLI
npm install -g heroku

# Login
heroku login

# Create app
heroku create your-app-name

# Set environment variables
heroku config:set FIREBASE_PROJECT_ID=arfu27-c34b3

# Deploy
git push heroku main

# View logs
heroku logs --tail
```

### AWS / Google Cloud

Similar deployment process - provide Node.js runtime, set environment variables, deploy code.

## File Structure

```
controller_server/
├── index.js              # Main server file
├── fcm.js                # FCM message sending
├── db.js                 # Firebase database operations
├── package.json
├── serviceAccountKey.json (create from Firebase)
├── .env                  (create from .env.example)
├── .env.example
├── .gitignore
├── CURL_EXAMPLES.sh
└── README.md
```

## Development

Watch for changes and auto-restart:

```bash
npm install -g nodemon
npm run dev
```

## Testing

```bash
# Test health endpoint
curl http://localhost:3000/health

# List all devices
curl http://localhost:3000/devices

# Send test USSD
curl -X POST http://localhost:3000/send-ussd \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test_device",
    "ussd_code": "*999#"
  }'
```

## Security

- ✅ Firebase authentication required
- ✅ Service account key stored securely
- ✅ Database rules restrict access
- ✅ No sensitive data in logs
- ✅ HTTPS recommended for production

## Troubleshooting

**Server won't start?**
- Check Node.js version: `node --version` (need 14+)
- Check serviceAccountKey.json exists
- Check Firebase project is active

**Apps can't connect?**
- Use local IP address instead of localhost
- Check firewall allows port 3000
- Check Firebase Realtime Database is running

**Commands not sending?**
- Verify FCM token is correct
- Check Firebase permissions
- See device in /devices endpoint

## For More Information

See [PROJECT_OVERVIEW.md](../DOCUMENTATION/PROJECT_OVERVIEW.md) for system architecture.
