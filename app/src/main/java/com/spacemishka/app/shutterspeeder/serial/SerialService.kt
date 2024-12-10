package com.spacemishka.app.shutterspeeder.serial

import android.hardware.usb.*
import android.util.Log
import com.spacemishka.app.shutterspeeder.data.AppPreferences
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

sealed class MeasurementState {
    object Idle : MeasurementState()
    object Measuring : MeasurementState()
    data class Success(val measurement: MeasurementData) : MeasurementState()
    data class Error(val message: String) : MeasurementState()
}

class SerialService(
    private val usbManager: UsbManager,
    private val preferences: AppPreferences
) {
    private var connection: UsbDeviceConnection? = null
    private var device: UsbDevice? = null
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null
    private var interface0: UsbInterface? = null
    private var readJob: Job? = null
    private val _measurementState = MutableStateFlow<MeasurementState>(MeasurementState.Idle)
    val measurementState: StateFlow<MeasurementState> = _measurementState.asStateFlow()
    
    companion object {
        private const val TAG = "SerialService"
        private const val TIMEOUT = 100
        private const val BUFFER_SIZE = 1024
        private const val BAUD_RATE = 9600
        private const val DISPLAY_READY_MESSAGE = "Display Ready"
        private const val MAX_CONSECUTIVE_ERRORS = 20
    }
    
    suspend fun connect(): Result<Unit> {
        return try {
            val deviceType = preferences.deviceType.first()
            Log.e(TAG, "Attempting to connect to device type: ${deviceType.displayName} (VID: ${deviceType.vendorId}, PID: ${deviceType.productId})")
            
            val devices = usbManager.deviceList
            Log.e(TAG, "Found ${devices.size} USB devices")
            devices.forEach { (key, device) ->
                Log.e(TAG, "USB Device: $key, VID: ${device.vendorId}, PID: ${device.productId}")
            }
            
            device = devices.values.find { 
                it.vendorId == deviceType.vendorId && it.productId == deviceType.productId 
            } ?: throw IOException("Device not found: ${deviceType.displayName}")
            
            Log.e(TAG, "Found matching device: ${device?.deviceName}, interfaces: ${device?.interfaceCount}")
            
            if (!usbManager.hasPermission(device)) {
                Log.e(TAG, "No permission for device, requesting permission...")
                throw IOException("No permission to access USB device. Please grant permission and try again.")
            }
            
            connection = usbManager.openDevice(device)
            if (connection == null) {
                Log.e(TAG, "Failed to open connection")
                throw IOException("Could not open connection. Please check USB permissions.")
            }
            Log.e(TAG, "Successfully opened USB connection")

            // Log all interfaces and endpoints first
            for (i in 0 until (device?.interfaceCount ?: 0)) {
                val iface = device?.getInterface(i)
                if (iface == null) {
                    Log.e(TAG, "Interface $i is null")
                    continue
                }
                
                Log.e(TAG, "Interface $i: class=${iface.interfaceClass}, subclass=${iface.interfaceSubclass}, " +
                         "protocol=${iface.interfaceProtocol}, endpoints=${iface.endpointCount}")
                
                for (j in 0 until iface.endpointCount) {
                    val endpoint = iface.getEndpoint(j)
                    val type = when(endpoint.type) {
                        UsbConstants.USB_ENDPOINT_XFER_BULK -> "BULK"
                        UsbConstants.USB_ENDPOINT_XFER_INT -> "INT"
                        UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "CONTROL"
                        UsbConstants.USB_ENDPOINT_XFER_ISOC -> "ISOC"
                        else -> "UNKNOWN"
                    }
                    val dir = when(endpoint.direction) {
                        UsbConstants.USB_DIR_IN -> "IN"
                        UsbConstants.USB_DIR_OUT -> "OUT"
                        else -> "UNKNOWN"
                    }
                    Log.e(TAG, "  Endpoint $j: type=$type, direction=$dir, address=0x${endpoint.address.toString(16)}, " +
                             "maxPacketSize=${endpoint.maxPacketSize}")
                }
            }

            // First claim the control interface (Interface 0)
            val controlInterface = device?.getInterface(0)
            if (controlInterface != null) {
                val claimed = connection?.claimInterface(controlInterface, true) ?: false
                Log.e(TAG, "Claimed control interface: $claimed")
                
                // Set control line state
                connection?.controlTransfer(
                    UsbConstants.USB_TYPE_CLASS or UsbConstants.USB_DIR_OUT,
                    0x22,  // SET_CONTROL_LINE_STATE
                    0x01,  // DTR ON
                    0,     // Interface 0
                    null,
                    0,
                    TIMEOUT
                )
            }

            // Then get the data interface (Interface 1)
            val dataInterface = device?.getInterface(1)
            if (dataInterface == null) {
                throw IOException("Could not get data interface")
            }

            // Find bulk endpoints on the data interface
            for (j in 0 until dataInterface.endpointCount) {
                val endpoint = dataInterface.getEndpoint(j)
                if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                        inEndpoint = endpoint
                        Log.e(TAG, "Found bulk IN endpoint: address=0x${endpoint.address.toString(16)}")
                    } else if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        outEndpoint = endpoint
                        Log.e(TAG, "Found bulk OUT endpoint: address=0x${endpoint.address.toString(16)}")
                    }
                }
            }

            if (inEndpoint == null || outEndpoint == null) {
                throw IOException("Could not find required bulk endpoints")
            }

            // Claim the data interface
            interface0 = dataInterface
            val claimed = connection?.claimInterface(dataInterface, true) ?: false
            Log.e(TAG, "Claimed data interface: $claimed")
            if (!claimed) {
                throw IOException("Failed to claim data interface")
            }

            // Set baud rate through the control interface
            val result = connection?.controlTransfer(
                UsbConstants.USB_TYPE_CLASS or UsbConstants.USB_DIR_OUT,
                0x20,  // SET_LINE_CODING
                0,
                0,
                byteArrayOf(
                    (BAUD_RATE and 0xff).toByte(),
                    (BAUD_RATE shr 8 and 0xff).toByte(),
                    (BAUD_RATE shr 16 and 0xff).toByte(),
                    (BAUD_RATE shr 24 and 0xff).toByte(),
                    0,  // 1 stop bit
                    0,  // no parity
                    8   // 8 data bits
                ),
                7,
                TIMEOUT
            ) ?: -1
            Log.e(TAG, "Set baud rate result: $result")
            
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to USB device", e)
            Result.failure(e)
        }
    }

    private fun startReading() {
        readJob?.cancel()
        readJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            val stringBuilder = StringBuilder()
            
            while (isActive) {
                try {
                    val bytesRead = connection?.bulkTransfer(
                        inEndpoint,
                        buffer.array(),
                        buffer.capacity(),
                        TIMEOUT
                    ) ?: -1

                    if (bytesRead > 0) {
                        val chunk = String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8)
                        Log.e(TAG, "Received data: $chunk")
                        stringBuilder.append(chunk)

                        if (chunk.contains("\n")) {
                            val jsonStr = stringBuilder.toString().trim()
                            stringBuilder.clear()

                            if (jsonStr.isNotEmpty()) {
                                try {
                                    val data = parseMeasurementJson(jsonStr)
                                    _measurementState.value = MeasurementState.Success(data)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing JSON: $jsonStr", e)
                                    _measurementState.value = MeasurementState.Error(e.message ?: "Invalid data format")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading from device", e)
                    _measurementState.value = MeasurementState.Error(e.message ?: "Read error")
                    delay(1000) // Wait before retrying
                }
            }
        }
    }
    
    fun disconnect() {
        try {
            readJob?.cancel()
            interface0?.let { connection?.releaseInterface(it) }
            connection?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        } finally {
            connection = null
            device = null
            inEndpoint = null
            outEndpoint = null
            interface0 = null
            _measurementState.value = MeasurementState.Idle
        }
    }
    
    fun reset() {
        _measurementState.value = MeasurementState.Idle
    }

    fun measure(): Flow<MeasurementData> = flow {
        try {
            // Reset state at the start of new measurement
            _measurementState.value = MeasurementState.Idle
            delay(100) // Short delay to ensure UI updates
            _measurementState.value = MeasurementState.Measuring
            
            Log.e(TAG, "Starting new measurement...")
            
            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            var deviceReady = false
            var messageCount = 0
            var readAttempts = 0
            var consecutiveErrors = 0
            val stringBuilder = StringBuilder()
            
            while (true) {
                readAttempts++
                if (readAttempts % 20 == 0) {
                    Log.e(TAG, "Read attempt $readAttempts, device ready: $deviceReady, message count: $messageCount, errors: $consecutiveErrors")
                }

                val bytesRead = connection?.bulkTransfer(
                    inEndpoint,
                    buffer.array(),
                    buffer.capacity(),
                    if (!deviceReady) 1000 else TIMEOUT
                ) ?: -1

                if (bytesRead > 0) {
                    consecutiveErrors = 0
                    val message = String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8)
                    Log.e(TAG, "Raw received data ($bytesRead bytes): '$message'")
                    stringBuilder.append(message)
                    
                    // Process complete messages
                    var processedMessage = false
                    while (stringBuilder.isNotEmpty()) {
                        val newlineIndex = stringBuilder.indexOf("\n")
                        if (newlineIndex == -1) break
                        
                        val completeMessage = stringBuilder.substring(0, newlineIndex).trim()
                        stringBuilder.delete(0, newlineIndex + 1)
                        
                        Log.e(TAG, "Processing complete message: '$completeMessage'")
                        processedMessage = true
                        
                        if (!deviceReady) {
                            Log.e(TAG, "Checking for Display Ready in: '$completeMessage'")
                            if (completeMessage.contains(DISPLAY_READY_MESSAGE, ignoreCase = true)) {
                                Log.e(TAG, "Display ready detected!")
                                deviceReady = true
                                messageCount = 0
                                continue
                            }
                        } else {
                            messageCount++
                            Log.e(TAG, "Message $messageCount received: '$completeMessage'")
                            
                            if (completeMessage.startsWith("{") && completeMessage.endsWith("}")) {
                                try {
                                    Log.e(TAG, "Found JSON message, attempting to parse")
                                    val data = parseMeasurementJson(completeMessage)
                                    Log.e(TAG, "Successfully parsed measurement data")
                                    emit(data)
                                    return@flow
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing JSON: $completeMessage", e)
                                }
                            }
                        }
                    }
                    
                    if (!processedMessage) {
                        Log.e(TAG, "Buffer contents (no newline found): '${stringBuilder}'")
                    }
                } else {
                    if (deviceReady) {
                        consecutiveErrors++
                    }
                    if (bytesRead < 0) {
                        Log.e(TAG, "Negative bytes read: $bytesRead (consecutive errors: $consecutiveErrors)")
                    }
                    delay(10)
                }
                
                if (!deviceReady) {
                    delay(50)
                } else {
                    delay(5)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during measurement", e)
            throw e
        } finally {
            // Reset state if measurement fails or completes
            if (_measurementState.value !is MeasurementState.Success) {
                _measurementState.value = MeasurementState.Idle
            }
        }
    }
    
    private fun sendCommand(command: Command) {
        if (outEndpoint == null) {
            Log.e(TAG, "Cannot send command: no OUT endpoint available")
            return
        }
        
        val buffer = ByteBuffer.allocate(1)
        buffer.put(command.value)
        
        val bytesSent = connection?.bulkTransfer(
            outEndpoint,
            buffer.array(),
            buffer.capacity(),
            TIMEOUT
        ) ?: -1
        
        Log.e(TAG, "Command sent: ${command.name}, bytes sent: $bytesSent")
        
        if (bytesSent < 0) {
            Log.e(TAG, "Failed to send command: $command")
            throw IOException("Failed to send command")
        }
    }
    
    private fun parseMeasurementJson(jsonStr: String): MeasurementData {
        val json = JSONObject(jsonStr)
        
        // Verify event type
        if (json.getString("eventType") != "MultiSensorMeasure") {
            throw IOException("Invalid event type: ${json.getString("eventType")}")
        }
        
        return MeasurementData(
            bottomLeftOpen = json.getLong("bottomLeftOpen"),
            bottomLeftClose = json.getLong("bottomLeftClose"),
            centerOpen = json.getLong("centerOpen"),
            centerClose = json.getLong("centerClose"),
            topRightOpen = json.getLong("topRightOpen"),
            topRightClose = json.getLong("topRightClose"),
            bottomLeftOpenOffset = json.getInt("bottomLeftOpenOffset"),
            bottomLeftCloseOffset = json.getInt("bottomLeftCloseOffset"),
            topRightOpenOffset = json.getInt("topRightOpenOffset"),
            topRightCloseOffset = json.getInt("topRightCloseOffset"),
            firmwareVersion = json.getString("firmware_version")
        )
    }
}

enum class Command(val value: Byte) {
    START_MEASUREMENT(0x01),
    STOP_MEASUREMENT(0x02),
    GET_FIRMWARE_VERSION(0x03)
}

data class MeasurementData(
    val bottomLeftOpen: Long,
    val bottomLeftClose: Long,
    val centerOpen: Long,
    val centerClose: Long,
    val topRightOpen: Long,
    val topRightClose: Long,
    val firmwareVersion: String,
    val bottomLeftOpenOffset: Int,
    val bottomLeftCloseOffset: Int,
    val topRightOpenOffset: Int,
    val topRightCloseOffset: Int,
) {
    fun toMeasurement(
        cameraId: Long = 0, 
        referenceShutterSpeed: String = "", 
        referenceSpeedMicros: Long = 0,
        selectedShutterSpeed: String = ""
    ): Measurement {
        return Measurement(
            cameraId = cameraId,
            bottomLeftOpen = bottomLeftOpen,
            bottomLeftClose = bottomLeftClose,
            centerOpen = centerOpen,
            centerClose = centerClose,
            topRightOpen = topRightOpen,
            topRightClose = topRightClose,
            bottomLeftOpenOffset = bottomLeftOpenOffset,
            bottomLeftCloseOffset = bottomLeftCloseOffset,
            topRightOpenOffset = topRightOpenOffset,
            topRightCloseOffset = topRightCloseOffset,
            firmwareVersion = firmwareVersion,
            referenceShutterSpeed = referenceShutterSpeed,
            referenceSpeedMicros = referenceSpeedMicros,
            selectedShutterSpeed = selectedShutterSpeed
        )
    }
}