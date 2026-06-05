const admin = require('firebase-admin');

async function sendUssdCommand(deviceId, ussdCode, autoExecute = false) {
    try {
        // Get device FCM token
        const deviceRef = admin.database().ref(`devices/${deviceId}`);
        const deviceSnapshot = await deviceRef.get();

        if (!deviceSnapshot.exists()) {
            throw new Error(`Device ${deviceId} not found`);
        }

        const fcmToken = deviceSnapshot.val().fcm_token;

        // Send FCM message
        const message = {
            data: {
                ussd_code: ussdCode,
                auto_execute: String(autoExecute),
                timestamp: String(Date.now())
            },
            token: fcmToken
        };

        const response = await admin.messaging().send(message);
        console.log(`Message sent to device ${deviceId}: ${response}`);

        return response;
    } catch (error) {
        console.error(`Error sending USSD command: ${error.message}`);
        throw error;
    }
}

module.exports = { sendUssdCommand };
