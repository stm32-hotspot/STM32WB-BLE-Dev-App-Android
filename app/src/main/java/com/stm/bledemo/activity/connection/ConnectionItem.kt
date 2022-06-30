package com.stm.bledemo.activity.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

data class ConnectionItem(
    val name: String,
    val type: String,
    val service: BluetoothGattService? = null,
    val characteristic: BluetoothGattCharacteristic? = null,
    val readable: Boolean = false,
    val writable: Boolean = false,
    val writableNoResponse: Boolean = false,
    val notifiable: Boolean = false,
    val indicatable: Boolean = false,
    var notify: Boolean = false,
    var notifyLabel: String = "Notify"
)
