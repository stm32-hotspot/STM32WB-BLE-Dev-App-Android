package com.stm.bledemo.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.stm.bledemo.BLEApplication
import com.stm.bledemo.activity.connection.ConnectionInterface
import com.stm.bledemo.activity.scan.ScanInterface
import com.stm.bledemo.activity.scan.ScanAdapter
import com.stm.bledemo.extension.toHexString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val GATT_MAX_MTU_SIZE = 517
private const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

@Suppress("unused")
@SuppressLint("NotifyDataSetChanged", "MissingPermission")
object BLEManager {

    var scanInterface: ScanInterface? = null
    var connectionInterface: ConnectionInterface? = null

    var bGatt: BluetoothGatt? = null
    var scanAdapter: ScanAdapter? = null

    // BLE Queue System (Coroutines)
    private val channel = Channel<BLEResult>()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var isScanning = false
    private var isConnected = false
    var deviceNameFilter = ""
    var deviceRSSIFilter = ""

    // List of BLE Scan Results
    val scanResults = mutableListOf<ScanResult>()

    val bAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            BLEApplication.app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner: BluetoothLeScanner by lazy {
        bAdapter.bluetoothLeScanner
    }

    /** Bluetooth 5 */

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkBluetooth5Support() {
        Timber.i("LE 2M PHY Supported: ${bAdapter.isLe2MPhySupported}")
        Timber.i("LE Coded PHY Supported: ${bAdapter.isLeCodedPhySupported}")
        Timber.i("LE Extended Advertising Supported: ${bAdapter.isLeExtendedAdvertisingSupported}")
        Timber.i("LE Periodic Advertising Supported: ${bAdapter.isLePeriodicAdvertisingSupported}")
    }

    /** BLE Scan */

    @SuppressLint("ObsoleteSdkInt")
    fun startScan(context: Context) {
        if (!hasPermissions(context)) {
            scanInterface?.requestPermissions()
        } else if (!isScanning) {
            scanResults.clear()
            scanAdapter?.notifyDataSetChanged()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkBluetooth5Support()
            }

            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            Timber.i("BLE Scan Started")
        }
    }

    fun stopScan() {
        if (isScanning) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
            Timber.i("BLE Scan Stopped")
        }
    }

    // Set Scan Settings (Low Latency High Power Usage)
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    // Scan Result Callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }

            if (indexQuery != -1) { // Updates Existing Scan Result
                scanResults[indexQuery] = result
                scanAdapter?.notifyItemChanged(indexQuery)
            } else { // Adds New Scan Result
                with(result.device) {
                    Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }

                // Check if Device Name & RSSI match filters
                val filterComparison =
                    scanAdapter?.filterCompare(result, deviceNameFilter, "name") == true &&
                    scanAdapter?.filterCompare(result, deviceRSSIFilter, "rssi") == true

                // Adds scanned device item to Recycler View if not filtered out
                if (filterComparison) {
                    scanResults.add(result)
                    scanAdapter?.notifyItemInserted(scanResults.size - 1)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("Scan Failed! Code: $errorCode")
        }
    }

    /** BLE Connection */

    // Connects to Scan Result Device
    fun connect(result: ScanResult, context: Context) {
        if (!isConnected) {
            stopScan()

            with(result.device) {
                connectGatt(context, false, gattCallback)
                Timber.i("Connecting to $address")
            }
        }
    }

    // Disconnects from Device
    fun disconnect() {
        if (isConnected) bGatt?.disconnect()
    }

    // Connection Callback
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device?.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnected = true
                    Timber.i("Successfully connected to $deviceAddress")

                    deviceNameFilter = ""
                    deviceRSSIFilter = ""

                    bGatt = gatt
                    scanInterface?.startIntent()

                    Handler(Looper.getMainLooper()).post {
                        bGatt!!.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    isConnected = false
                    Timber.i("Successfully disconnected from $deviceAddress")
                    connectionInterface?.finishActivity()
                    gatt.close()
                }
            } else {
                isConnected = false
                val message = "Connection Attempt Failed for $deviceAddress! Error: $status"
                Timber.e(message)
                scanInterface?.startToast(message)

                connectionInterface?.finishActivity()
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Timber.i("Discovered ${services.size} services for ${device.address}")
                printGattTable()

                connectionInterface?.addDiscoveredItems()

                scope.launch {
                    requestMTU(GATT_MAX_MTU_SIZE)
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Timber.i("ATT MTU changed to $mtu, Success: ${status == BluetoothGatt.GATT_SUCCESS}")
            channel.offer(BLEResult("MTU", null, status))
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        connectionInterface?.valueUpdated(characteristic)
                        Timber.i("Read characteristic $uuid:\n${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Timber.e("Read not permitted for $uuid!")
                    }
                    else -> {
                        Timber.e("Characteristic read failed for $uuid, Error: $status")
                    }
                }

                channel.offer(BLEResult(characteristic.uuid.toString(), value, status))
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Timber.i("Wrote to characteristic ${this.uuid} | value: ${this.value?.toHexString()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Timber.e("Write exceeded connectionInterface ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Timber.e("Write not permitted for ${this.uuid}!")
                    }
                    else -> {
                        Timber.e("Characteristic write failed for ${this.uuid}, error: $status")
                    }
                }

                channel.offer(BLEResult(characteristic.uuid.toString(), value, status))
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                connectionInterface?.valueUpdated(characteristic)
                Timber.i("Characteristic ${this.uuid} changed | value: ${this.value?.toHexString()}")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with (descriptor) {
                channel.offer(BLEResult(uuid.toString(), value, status))
            }
        }
    } // End of Connection Callback

    // Prints UUIDs of Available services & characteristics from Bluetooth Gatt
    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Timber.i("No service and characteristic available, call discoverServices() first?")
            return
        }

        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }

            Timber.i("\nService: ${service.uuid}\nCharacteristics:\n$characteristicsTable")
        }
    }

    // Request to Change MTU Size
    suspend fun requestMTU(size: Int): BLEResult? {
        bGatt?.requestMtu(size)
        return waitForResult("MTU")
    }

    /** Characteristic (Read/Write) */

    // Get a Characteristic using Service & Characteristic UUIDs
    private fun getCharacteristic(
        serviceUUIDString: String, characteristicUUIDString: String
    ): BluetoothGattCharacteristic? {
        val serviceUUID = UUID.fromString(serviceUUIDString)
        val characteristicUUID = UUID.fromString(characteristicUUIDString)

        return bGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
    }

    // Read from a Characteristic
    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic?): BLEResult? {
        if (characteristic!= null && characteristic.isReadable()) {
            bGatt?.readCharacteristic(characteristic)
        } else error("Characteristic ${characteristic?.uuid} cannot be read")
        return waitForResult(characteristic.uuid.toString())
    }

    // Writes to a Characteristic
    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?, payload: ByteArray): BLEResult? {
        val writeType = when {
            characteristic?.isWritable() == true -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic?.isWritableWithoutResponse() == true -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic?.uuid} cannot be written to")
        }

        bGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
        return waitForResult(characteristic.uuid.toString())
    }

    /** Notifications / Indications */

    suspend fun enableNotifications(characteristic: BluetoothGattCharacteristic?): BLEResult? {
        val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
        val payload = when {
            characteristic?.isIndicatable() == true -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic?.isNotifiable() == true -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Timber.e("${characteristic?.uuid} doesn't support notifications/indications")
                return null
            }
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (bGatt?.setCharacteristicNotification(characteristic, true) == false) {
                Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                return null
            }
            writeDescriptor(cccDescriptor, payload)
            return waitForResult(cccDescriptor.uuid.toString())
        } ?: Timber.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
        return null
    }

    suspend fun disableNotifications(characteristic: BluetoothGattCharacteristic?): BLEResult? {
        if (characteristic != null) {
            if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
                Timber.e("${characteristic.uuid} doesn't support indications/notifications")
                return null
            }
        }

        val cccdUUID = UUID.fromString(CCC_DESCRIPTOR_UUID)
        characteristic?.getDescriptor(cccdUUID)?.let { cccDescriptor ->
            if (bGatt?.setCharacteristicNotification(characteristic, false) == false) {
                Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                return null
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            return waitForResult(cccDescriptor.uuid.toString())
        } ?: Timber.e("${characteristic?.uuid} doesn't contain the CCC descriptor")
        return null
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        bGatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    /** Bonding (Not in Use) */

    fun createBond(device: BluetoothDevice) {
        // Gatt.device
        device.createBond()
    }

    fun removeBond(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond")
            val result: Boolean = method.invoke(device) as Boolean

            if (result) Timber.i("Successfully removed bond!")
        } catch (e: Exception) {
            Timber.e("Error: could not remove bond!")
        }
    }

    /** Helper Functions */

    fun hasPermissions(context: Context): Boolean {
        return hasLocationPermission(context) && hasBluetoothPermission(context)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun hasLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBluetoothPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    // Wait for BLE Operation Result
    private suspend fun waitForResult(id: String): BLEResult? {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(5)) {
            var bleResult: BLEResult = channel.receive()
            while (bleResult.id != id) {
                bleResult = channel.receive()
            }
            bleResult
        } ?: run {
            //throw BLETimeoutException("BLE Operation Timed Out!")
            Timber.e("BLE Operation Timed Out!")
            return null
        }
    }
}