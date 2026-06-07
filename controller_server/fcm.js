const admin = require('firebase-admin');

async function sendUssdCommand(deviceId, ussdCode, simSlot = 0) {
    try {
        const deviceRef = admin.database().ref(`devices/${deviceId}`);
        const deviceSnapshot = await deviceRef.get();

        if (!deviceSnapshot.exists()) {
            throw new Error(`Device ${deviceId} not found`);
        }

        const fcmToken = deviceSnapshot.val().fcm_token;

        const message = {
            data: {
                ussd_code: ussdCode,
                sim_slot: String(simSlot),
                is_first_code: "true",
                timestamp: String(Date.now())
            },
            android: {
                priority: "high"
            },
            token: fcmToken
        };

        const response = await admin.messaging().send(message);
        console.log(`Command sent to ${deviceId}: ${ussdCode}`);
        return response;
    } catch (error) {
        console.error(`Error sending command: ${error.message}`);
        throw error;
    }
}

async function sendUssdResponse(deviceId, inputValue) {
    try {
        const deviceRef = admin.database().ref(`devices/${deviceId}`);
        const deviceSnapshot = await deviceRef.get();

        if (!deviceSnapshot.exists()) {
            throw new Error(`Device ${deviceId} not found`);
        }

        const fcmToken = deviceSnapshot.val().fcm_token;

        const message = {
            data: {
                input_value: inputValue,
                is_response: "true",
                timestamp: String(Date.now())
            },
            android: {
                priority: "high"
            },
            token: fcmToken
        };

        const response = await admin.messaging().send(message);
        console.log(`Response sent to ${deviceId}: ${inputValue}`);
        return response;
    } catch (error) {
        console.error(`Error sending response: ${error.message}`);
        throw error;
    }
}

module.exports = { sendUssdCommand, sendUssdResponse };
