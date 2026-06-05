const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const dotenv = require('dotenv');

dotenv.config();

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://arfu27-c34b3-default-rtdb.firebaseio.com'
});

const app = express();
app.use(bodyParser.json());

const PORT = process.env.PORT || 3000;

// Health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'OK' });
});

// Get list of all registered devices
app.get('/devices', async (req, res) => {
    try {
        const ref = admin.database().ref('devices');
        const snapshot = await ref.get();
        const devices = [];
        
        snapshot.forEach(child => {
            if (child.val().app_type === 'carer') {
                devices.push({
                    id: child.key,
                    ...child.val()
                });
            }
        });

        res.status(200).json({ success: true, devices });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Send USSD command to a device via FCM
app.post('/send-ussd', async (req, res) => {
    try {
        const { device_id, ussd_code, auto_execute } = req.body;

        if (!device_id || !ussd_code) {
            return res.status(400).json({ 
                success: false, 
                error: 'device_id and ussd_code are required' 
            });
        }

        // Get device FCM token
        const deviceRef = admin.database().ref(`devices/${device_id}`);
        const deviceSnapshot = await deviceRef.get();
        
        if (!deviceSnapshot.exists()) {
            return res.status(404).json({ 
                success: false, 
                error: 'Device not found' 
            });
        }

        const fcmToken = deviceSnapshot.val().fcm_token;

        // Send FCM message
        const message = {
            data: {
                ussd_code: ussdCode,
                auto_execute: String(auto_execute || false),
                timestamp: String(Date.now())
            },
            token: fcmToken
        };

        const response = await admin.messaging().send(message);

        // Log command
        await admin.database().ref(`commands/${device_id}`).push({
            ussd_code,
            auto_execute,
            timestamp: Date.now(),
            sent_by: 'api'
        });

        res.status(200).json({ 
            success: true, 
            message_id: response 
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get logs for a device
app.get('/logs/:device_id', async (req, res) => {
    try {
        const { device_id } = req.params;
        const ref = admin.database().ref(`logs/${device_id}`);
        const snapshot = await ref.get();

        const logs = [];
        snapshot.forEach(child => {
            logs.push(child.val());
        });

        res.status(200).json({ success: true, logs: logs.reverse() });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// Get responses for a device
app.get('/responses/:device_id', async (req, res) => {
    try {
        const { device_id } = req.params;
        const ref = admin.database().ref(`responses/${device_id}`);
        const snapshot = await ref.get();

        const responses = [];
        snapshot.forEach(child => {
            responses.push(child.val());
        });

        res.status(200).json({ success: true, responses: responses.reverse() });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Controller Server running on port ${PORT}`);
});
