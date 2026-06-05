const admin = require('firebase-admin');

async function logCommand(deviceId, ussdCode, status, error = '') {
    try {
        await admin.database().ref(`logs/${deviceId}`).push({
            ussd_code: ussdCode,
            status,
            error,
            timestamp: new Date().toISOString(),
            time_ms: Date.now()
        });
    } catch (err) {
        console.error(`Error logging command: ${err.message}`);
    }
}

async function logResponse(deviceId, response) {
    try {
        await admin.database().ref(`responses/${deviceId}`).push({
            response,
            timestamp: new Date().toISOString(),
            time_ms: Date.now()
        });
    } catch (err) {
        console.error(`Error logging response: ${err.message}`);
    }
}

async function getAllDevices() {
    try {
        const ref = admin.database().ref('devices');
        const snapshot = await ref.get();
        const devices = [];

        snapshot.forEach(child => {
            devices.push({
                id: child.key,
                ...child.val()
            });
        });

        return devices;
    } catch (err) {
        console.error(`Error getting devices: ${err.message}`);
        return [];
    }
}

async function getDeviceById(deviceId) {
    try {
        const ref = admin.database().ref(`devices/${deviceId}`);
        const snapshot = await ref.get();
        
        if (!snapshot.exists()) {
            return null;
        }

        return {
            id: deviceId,
            ...snapshot.val()
        };
    } catch (err) {
        console.error(`Error getting device: ${err.message}`);
        return null;
    }
}

module.exports = {
    logCommand,
    logResponse,
    getAllDevices,
    getDeviceById
};
