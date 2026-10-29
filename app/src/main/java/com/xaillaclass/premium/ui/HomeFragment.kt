package com.xaillaclass.premium.ui

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xaillaclass.premium.R
import com.xaillaclass.premium.v2ray.V2RayService

class HomeFragment : Fragment() {

    private var isConnected = false
    private val PREF_NAME = "xaillaclass_settings"
    private val KEY_VPN_ACTIVE = "vpn_active"

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(requireContext(), "Izin VPN ditolak", Toast.LENGTH_SHORT).show()
            resetButton()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        isConnected = prefs.getBoolean(KEY_VPN_ACTIVE, false)

        // Cek status saat fragment dimuat ulang
        updateUi(view)

        btnConnect.setOnClickListener {
            if (!isConnected) {
                val intent = VpnService.prepare(requireContext())
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    startVpnService()
                }
            } else {
                stopVpnService()
            }
        }
        return view
    }

    private fun updateUi(view: View) {
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        if (isConnected) {
            tvStatus.text = getString(R.string.status_connected)
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_success))
            btnConnect.text = getString(R.string.disconnect)
            btnConnect.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_error))
        } else {
            tvStatus.text = getString(R.string.status_disconnected)
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
            btnConnect.text = getString(R.string.connect)
            btnConnect.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary))
        }
    }

    private fun startVpnService() {
        isConnected = true
        saveState(true)
        updateUi(requireView())

        val intent = Intent(requireContext(), V2RayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun stopVpnService() {
        isConnected = false
        saveState(false)
        updateUi(requireView())

        val intent = Intent(requireContext(), V2RayService::class.java)
        intent.action = "STOP"
        requireContext().startService(intent)
    }

    private fun saveState(state: Boolean) {
        requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_VPN_ACTIVE, state).apply()
    }

    private fun resetButton() {
        isConnected = false
        saveState(false)
        updateUi(requireView())
    }
}
