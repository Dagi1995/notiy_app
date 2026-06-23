const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const dotenv = require('dotenv');
const { sendUssdCommand, sendUssdResponse } = require('./fcm');

dotenv.config();

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://notify-app-d734f-default-rtdb.firebaseio.com'
});

const app = express();
app.use(bodyParser.json());

const PORT = process.env.PORT || 3000;

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'OK' });
});

app.get('/devices', async (req, res) => {
    try {
        const ref = admin.database().ref('devices');
        const snapshot = await ref.get();
        const devices = [];
        snapshot.forEach(child => {
            if (child.val().app_type === 'carer') {
                devices.push({ id: child.key, ...child.val() });
            }
        });
        res.status(200).json({ success: true, devices });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.post('/send-ussd', async (req, res) => {
    try {
        const { device_id, ussd_code, sim_slot } = req.body;
        if (!device_id || !ussd_code) {
            return res.status(400).json({ success: false, error: 'device_id and ussd_code are required' });
        }

        const messageId = await sendUssdCommand(device_id, ussd_code, sim_slot || 0);

        await admin.database().ref(`commands/${device_id}`).push({
            ussd_code,
            sim_slot: sim_slot || 0,
            is_first_code: "true",
            timestamp: Date.now(),
            sent_by: 'api'
        });

        res.status(200).json({ success: true, message_id: messageId });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.post('/send-response', async (req, res) => {
    try {
        const { device_id, input_value } = req.body;
        if (!device_id || !input_value) {
            return res.status(400).json({ success: false, error: 'device_id and input_value are required' });
        }

        const messageId = await sendUssdResponse(device_id, input_value);

        await admin.database().ref(`commands/${device_id}`).push({
            input_value,
            is_response: "true",
            timestamp: Date.now(),
            sent_by: 'api'
        });

        res.status(200).json({ success: true, message_id: messageId });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.get('/logs/:device_id', async (req, res) => {
    try {
        const { device_id } = req.params;
        const snapshot = await admin.database().ref(`logs/${device_id}`).get();
        const logs = [];
        snapshot.forEach(child => logs.push(child.val()));
        res.status(200).json({ success: true, logs: logs.reverse() });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Controller Server running on port ${PORT}`);
});
