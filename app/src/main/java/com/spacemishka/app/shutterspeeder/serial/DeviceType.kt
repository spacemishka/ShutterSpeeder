package com.spacemishka.app.shutterspeeder.serial

enum class DeviceType(val displayName: String, val vendorId: Int, val productId: Int) {
    RASPBERRY_PICO("Raspberry Pico", 0x2E8A, 0x000A),
    STM32("STM32", 0x0483, 0x5740),// Standard Pico USB VID/PID
    ARDUINO("Arduino", 0x2341, 0x0043);  // Standard Arduino Uno R3 VID/PID

    companion object {
        fun fromDisplayName(name: String): DeviceType? = values().find { it.displayName == name }
    }
} 