package com.stm.bledemo.activity.scan.fragment

import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.stm.bledemo.R
import com.stm.bledemo.ble.GAP
import com.stm.bledemo.databinding.FragmentAdvertisingDataBinding
import com.stm.bledemo.extension.hexToASCII
import com.stm.bledemo.extension.toHexString
import org.jetbrains.anko.padding
import timber.log.Timber

class AdvertisingDataFragment(private val result: ScanResult): DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentAdvertisingDataBinding>(
            inflater,
            R.layout.fragment_advertising_data,
            container,
            false
        )
        var rawData = result.scanRecord?.bytes?.toHexString()?.uppercase() ?: ""
        rawData = rawData.substring(2, rawData.length) // Remove leading 0x

        val advertisingData = rawData.substring(0, 93)
        val scanResponseData = rawData.substring(93, rawData.length)

        with(binding) {
            advertisingDataText.text = advertisingData
            scanResponseDataText.text = getString(R.string.no_response)

            for (i in scanResponseData) {
                if (i != '0' && i != ' ') {
                    scanResponseDataText.text = scanResponseData
                }
            }

            Timber.i("Raw Data: $rawData")
            Timber.i("Advertising Data: $advertisingData")
            Timber.i("Response Data: $scanResponseData")
            rawData = rawData.filter { !it.isWhitespace() }

            // Parse Raw Data for Details
            var index = 0
            while (index < rawData.length) {
                // Create New Table Row
                val tableRow = TableRow(activity).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                // Length Value & TextView
                var len = rawData.substring(index, index + 2).toLong(radix = 16).toInt()
                val lenTV = createTextView(len.toString(), R.drawable.ic_blr_cell_shape, 0.12f)
                tableRow.addView(lenTV)

                if (len == 0) {
                    break
                }

                index += 2
                len *= 2

                var eirPacket = ""
                for (i in index until (index + len)) {
                    eirPacket += rawData[i]
                }

                // Type & Value TextViews
                if (eirPacket.isNotEmpty()) {
                    val gapType = GAP.getGAPType(eirPacket.substring(0, 2))

                    // Setup Type String
                    val type = if (gapType.isNotEmpty()) {
                        gapType + " (${eirPacket.substring(0, 2)})"
                    } else {
                        eirPacket.substring(0, 2)
                    }

                    // Setup Value String
                    val value = if (gapType == "CLName" || gapType == "SLName") {
                        eirPacket.substring(2, eirPacket.length).hexToASCII() +
                        " (${eirPacket.substring(2, eirPacket.length)})"
                    } else {
                        eirPacket.substring(2, eirPacket.length)
                    }

                    // Create Text Views
                    val typeTV = createTextView(
                        type,
                        R.drawable.ic_br_cell_shape,
                        0.15f
                    )
                    val valueTV = createTextView(
                        value,
                        R.drawable.ic_br_cell_shape,
                        0.4f
                    )

                    tableRow.addView(typeTV)
                    tableRow.addView(valueTV)
                }

                // Add Row to Table
                detailsTable.addView(tableRow)

                index += len
            }

            // Makes URL Link Clickable
            detailsDesc.movementMethod = LinkMovementMethod()

            okButton.setOnClickListener {
                dismiss()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // Set Fragment Dimensions
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    private fun createTextView(sText: String, drawID: Int, weight: Float): TextView {
        return TextView(activity).apply {
            text = sText
            textSize = 18f
            padding = 10
            setBackgroundResource(drawID)
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.MATCH_PARENT,
                weight
            )
            gravity = Gravity.FILL
        }
    }
}