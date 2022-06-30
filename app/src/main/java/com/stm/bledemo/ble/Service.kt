package com.stm.bledemo.ble

enum class Service (
    val sName: String,
    val UUID: String
) {
    HEARTRATE   ("Heart Rate Service", "0000180d-0000-1000-8000-00805f9b34fb"),
    P2PSERVER   ("P2P Server", "0000fe40-cc7a-482a-984a-7f2ed5b3e58f"),
    GENATT      ("Generic Attribute", "00001801-0000-1000-8000-00805f9b34fb"),
    GENACCESS   ("Generic Access", "00001800-0000-1000-8000-00805f9b34fb"),
    DEVINFO     ("Device Information Service", "0000180a-0000-1000-8000-00805f9b34fb");

    companion object {
        private val serviceMap = mapOf(
            Pair(HEARTRATE.UUID, HEARTRATE),
            Pair(P2PSERVER.UUID, P2PSERVER),
            Pair(GENATT.UUID, GENATT),
            Pair(GENACCESS.UUID, GENACCESS),
            Pair(DEVINFO.UUID, DEVINFO)
        )

        fun getServiceName(UUID: String): String {
            return if (serviceMap.containsKey(UUID)) {
                serviceMap[UUID]!!.sName
            } else {
                "Service"
            }
        }
    }
}