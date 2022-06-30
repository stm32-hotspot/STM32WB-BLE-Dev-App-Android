package com.stm.bledemo.ble

enum class Characteristic (
    val cName: String,
    val UUID: String,
    val sUUID: String
){
    // Heart Rate Service
    BODYSENLOC ("Body Sensor Location", "00002a38-0000-1000-8000-00805f9b34fb", "0000180d-0000-1000-8000-00805f9b34fb"),
    HRCONTROL ("Heart Rate Control Point", "00002a39-0000-1000-8000-00805f9b34fb", "0000180d-0000-1000-8000-00805f9b34fb"),
    HRMEASURE ("Heart Rate Measurement", "00002a37-0000-1000-8000-00805f9b34fb", "0000180d-0000-1000-8000-00805f9b34fb"),
    // Generic Attribute Service
    SERCHANGE ("Service Changed", "00002a05-0000-1000-8000-00805f9b34fb", "00001801-0000-1000-8000-00805f9b34fb"),
    // Generic Access Service
    DEVNAME ("Device Name", "00002a00-0000-1000-8000-00805f9b34fb", "00001800-0000-1000-8000-00805f9b34fb"),
    APPEAR ("Appearance", "00002a01-0000-1000-8000-00805f9b34fb", "00001800-0000-1000-8000-00805f9b34fb"),
    PPCP ("Peripheral Preferred Connection Parameters", "00002a04-0000-1000-8000-00805f9b34fb", "00001800-0000-1000-8000-00805f9b34fb"),
    // Device Information Service
    MFRNAME ("Manufacturer Name", "00002a29-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    // P2P Server Service
    P2PWRITE ("P2P Write", "0000fe41-8e22-4541-9d4c-21edae82ed19", "0000fe40-cc7a-482a-984a-7f2ed5b3e58f"),
    P2PNOTIFY ("P2P Notify", "0000fe42-8e22-4541-9d4c-21edae82ed19", "0000fe40-cc7a-482a-984a-7f2ed5b3e58f");

    companion object {
        private val charMap = mapOf(
            Pair(BODYSENLOC.UUID, BODYSENLOC),
            Pair(HRCONTROL.UUID, HRCONTROL),
            Pair(HRMEASURE.UUID, HRMEASURE),
            Pair(SERCHANGE.UUID, SERCHANGE),
            Pair(DEVNAME.UUID, DEVNAME),
            Pair(APPEAR.UUID, APPEAR),
            Pair(PPCP.UUID, PPCP),
            Pair(MFRNAME.UUID, MFRNAME),
            Pair(P2PWRITE.UUID, P2PWRITE),
            Pair(P2PNOTIFY.UUID, P2PNOTIFY)
        )

        fun getCharacteristicName(cUUID: String, sUUID: String): String {
            return if (charMap.containsKey(cUUID) && charMap[cUUID]!!.sUUID == sUUID) {
                charMap[cUUID]!!.cName
            } else {
                "Characteristic"
            }
        }
    }
}