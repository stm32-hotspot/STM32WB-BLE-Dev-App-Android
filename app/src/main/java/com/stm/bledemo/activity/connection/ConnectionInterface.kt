package com.stm.bledemo.activity.connection

import android.bluetooth.BluetoothGattCharacteristic

interface ConnectionInterface {
    fun addDiscoveredItems()
    fun valueUpdated(characteristic: BluetoothGattCharacteristic)
    fun finishActivity()
}