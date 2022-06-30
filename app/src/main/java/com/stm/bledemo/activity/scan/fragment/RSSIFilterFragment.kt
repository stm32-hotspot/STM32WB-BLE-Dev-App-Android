package com.stm.bledemo.activity.scan.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.FragmentRssiFilterBinding
import com.google.android.material.slider.Slider

class RSSIFilterFragment: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentRssiFilterBinding>(
            inflater,
            R.layout.fragment_rssi_filter,
            container,
            false
        )

        with(binding) {
            // Set Slider Value if previously changed
            if (BLEManager.deviceRSSIFilter.isNotEmpty()) {
                rssiSlider.value = BLEManager.deviceRSSIFilter.toFloat() * -1
            }

            // Change Slider Label (Negative & Int)
            rssiSlider.setLabelFormatter { value: Float ->
                var newValue = value.toInt()

                if (value != 0f) {
                    newValue *= -1
                }

                "$newValue dBm"
            }

            // RSSI Slider Thumb Moved
            rssiSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) {
                    // Responds to when slider's touch event is being started
                }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {
                    // Responds to when slider's touch event is being stopped
                    val newValue = slider.value.toInt() * -1

                    if (newValue == -125) {
                        BLEManager.deviceRSSIFilter = ""
                    } else {
                        BLEManager.scanAdapter?.filter(newValue.toString(), "rssi")
                    }
                }
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // Change Dialog Window Position
        val window: Window? = dialog?.window
        val params: WindowManager.LayoutParams? = window?.attributes
        window?.setGravity(Gravity.TOP)
        params?.y = 100

        // Set Fragment Dimensions
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.setLayout(width, height)
    }

}