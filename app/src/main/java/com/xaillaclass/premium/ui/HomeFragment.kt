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
import com.xaillaclass.premium.v2ray.V2RayService

class HomeFragment : Fragment() {

    private var isConnected = false

    // Launcher untuk menangkap hasil dialog izin VPN
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // User menyetujui izin VPN
            startVpnService()
        } else {
            // User menolak izin VPN
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

        btnConnect.setOnClickListener {
            if (!isConnected) {
                // Minta Izin VPN (Wajib di Android)
                val intent = VpnService.prepare(requireContext())
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    // Jika sudah pernah izinkan sebelumnya
                    startVpnService()
                }
            } else {
                stopVpnService()
            }
        }
        return view
    }

    private fun startVpnService() {
        isConnected = true
        val view = view ?: return
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        tvStatus.text = getString(R.string.status_connected)
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_success))
        btnConnect.text = getString(R.string.disconnect)
        btnConnect.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_error))

        val intent = Intent(requireContext(), V2RayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun stopVpnService() {
        isConnected = false
        val view = view ?: return
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        tvStatus.text = getString(R.string.status_disconnected)
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        btnConnect.text = getString(R.string.connect)
        btnConnect.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary))

        val intent = Intent(requireContext(), V2RayService::class.java)
        intent.action = "STOP"
        requireContext().startService(intent)
    }

    private fun resetButton() {
        isConnected = false
        val view = view ?: return
        view.findViewById<Button>(R.id.btnConnect).text = getString(R.string.connect)
    }
}
