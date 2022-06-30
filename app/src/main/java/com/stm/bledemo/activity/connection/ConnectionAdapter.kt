package com.stm.bledemo.activity.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.RowConnectionBinding
import com.stm.bledemo.extension.hexToByteArray
import com.stm.bledemo.extension.removeWhiteSpace
import com.stm.bledemo.extension.toHexString
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
class ConnectionAdapter : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    private val items: ArrayList<ConnectionItem> = arrayListOf()

    inner class ViewHolder(val binding: RowConnectionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            with (binding) {
                // Read Button Clicked
                readButton.setOnClickListener {
                    BLEManager.scope.launch {
                        val item = items[bindingAdapterPosition]
                        BLEManager.readCharacteristic(item.characteristic)
                    }
                }

                // Write Button Clicked (Accepts Hex Message, Sends Byte Array)
                writeButton.setOnClickListener {
                    BLEManager.scope.launch {
                        if (writeEditText.text.isNotEmpty()) {
                            val item = items[bindingAdapterPosition]
                            val byteMessage = writeEditText.text.toString().removeWhiteSpace().hexToByteArray()
                            BLEManager.writeCharacteristic(item.characteristic, byteMessage)
                        }

                        writeEditText.setText("")
                    }
                }

                // Notify Switch Toggled
                notifySwitch.setOnCheckedChangeListener { _, isChecked ->
                    BLEManager.scope.launch {
                        val item = items[bindingAdapterPosition]

                        if (isChecked) {
                            BLEManager.enableNotifications(item.characteristic)
                            item.notify = true
                        } else {
                            BLEManager.disableNotifications(item.characteristic)
                            item.notify = false
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowConnectionBinding>(
            inflater,
            R.layout.row_connection,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        with(holder.binding) {
            itemName.text = item.name

            if (item.indicatable) item.notifyLabel = "Indicate"
            notifySwitch.text = item.notifyLabel
            notifySwitch.isChecked = item.notify

            readLayout.isGone = !item.readable
            writeLayout.isGone = !(item.writable || item.writableNoResponse)
            notifyLayout.isGone = !(item.notifiable || item.indicatable)

            if (item.type == "Characteristic") {
                itemUUID.text = item.characteristic?.uuid
                    .toString()
                    .uppercase()
                    .substring(4, 8)
                readValue.text = item.characteristic?.value?.toHexString() ?: ""
                notifyValue.text = item.characteristic?.value?.toHexString() ?: ""
            } else {
                itemUUID.text = item.service?.uuid
                    .toString()
                    .uppercase()
                    .substring(4, 8)
            }
        }
    }

    override fun getItemCount() = items.size

    fun addItemList(itemList: List<ConnectionItem>) {
        items.addAll(itemList)
        notifyDataSetChanged()
    }

    fun valueUpdated(characteristic: BluetoothGattCharacteristic) {
        val index = items.indexOfFirst {
            it.characteristic != null && it.characteristic == characteristic
        }

        if (index != -1) {
            notifyItemChanged(index)
        }
    }
}