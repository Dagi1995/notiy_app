#!/bin/bash
# Example USSD commands using curl

# Get health status
curl -X GET http://localhost:3000/health

# Get list of devices
curl -X GET http://localhost:3000/devices

# Send USSD command (*127# - Telebirr balance)
curl -X POST http://localhost:3000/send-ussd \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "arfu_001",
    "ussd_code": "*127#",
    "auto_execute": false
  }'

# Send USSD with input (e.g., select option 1)
curl -X POST http://localhost:3000/send-ussd \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "arfu_001",
    "ussd_code": "*127*1#",
    "auto_execute": false
  }'

# Get logs for a device
curl -X GET http://localhost:3000/logs/arfu_001

# Get responses for a device
curl -X GET http://localhost:3000/responses/arfu_001
