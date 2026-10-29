package com.xaillaclass.premium.ui
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xaillaclass.premium.R
import com.xaillaclass.premium.v2ray.V2RayService
class HomeFragment : Fragment() {
    private var isConnected = false
    private val reqPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startVpn() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        view.findViewById<Button>(R.id.btnConnect).setOnClickListener {
            if (!isConnected) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    reqPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else { startVpn() }
            } else { stopVpn() }
        }
        return view
    }
    private fun startVpn() {
        isConnected = true; val v = view ?: return
        v.findViewById<TextView>(R.id.tvStatus).apply {
            text = getString(R.string.status_connected); setTextColor(ContextCompat.getColor(requireContext(), R.color.green_success))
        }
        v.findViewById<Button>(R.id.btnConnect).apply {
            text = getString(R.string.disconnect); setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_error))
        }
        val intent = Intent(requireContext(), V2RayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireContext().startForegroundService(intent) else requireContext().startService(intent)
    }
    private fun stopVpn() {
        isConnected = false; val v = view ?: return
        v.findViewById<TextView>(R.id.tvStatus).apply {
            text = getString(R.string.status_disconnected); setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        }
        v.findViewById<Button>(R.id.btnConnect).apply {
            text = getString(R.string.connect); setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary))
        }
        requireContext().stopService(Intent(requireContext(), V2RayService::class.java))
    }
}
