package com.stm.bledemo.activity.scan.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager.bAdapter
import com.stm.bledemo.databinding.FragmentDeviceInfoBinding

class DeviceInfoFragment: DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentDeviceInfoBinding>(
            inflater,
            R.layout.fragment_device_info,
            container,
            false
        )

        with(binding) {
            deviceNameText.text = Settings.Secure.getString(
                activity?.contentResolver, "bluetooth_name"
            )
            modelText.text = Build.MODEL

            if (bAdapter.isLe2MPhySupported) phy2MText.text = "YES"
            if (bAdapter.isLeCodedPhySupported) phyCodedText.text = "YES"
            if (bAdapter.isLeExtendedAdvertisingSupported) extAdvText.text = "YES"
            if (bAdapter.isLePeriodicAdvertisingSupported) perAdvText.text = "YES"
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

}