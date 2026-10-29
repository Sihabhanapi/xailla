package com.xaillaclass.premium.ui

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
import com.xaillaclass.premium.util.PrefsHelper
import com.xaillaclass.premium.v2ray.V2RayService

class HomeFragment : Fragment() {
    private val vpnPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) startVpnService()
        else Toast.makeText(requireContext(), "Izin VPN ditolak", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        updateUi(view, PrefsHelper.isVpnActive(requireContext()))

        btnConnect.setOnClickListener {
            val isConnected = PrefsHelper.isVpnActive(requireContext())
            if (!isConnected) {
                if (PrefsHelper.getSelected(requireContext()) == null) {
                    Toast.makeText(requireContext(), "Pilih config terlebih dahulu!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val intent = VpnService.prepare(requireContext())
                if (intent != null) vpnPermissionLauncher.launch(intent)
                else startVpnService()
            } else stopVpnService()
        }
        return view
    }

    private fun startVpnService() {
        val intent = Intent(requireContext(), V2RayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireContext().startForegroundService(intent) else requireContext().startService(intent)
        PrefsHelper.setVpnActive(requireContext(), true)
        updateUi(requireView(), true)
    }

    private fun stopVpnService() {
        val intent = Intent(requireContext(), V2RayService::class.java).apply { action = "STOP" }
        requireContext().startService(intent)
        PrefsHelper.setVpnActive(requireContext(), false)
        updateUi(requireView(), false)
    }

    private fun updateUi(view: View, isConnected: Boolean) {
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
}
