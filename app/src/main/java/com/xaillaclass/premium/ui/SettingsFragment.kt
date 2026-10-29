package com.xaillaclass.premium.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.xaillaclass.premium.BuildConfig
import com.xaillaclass.premium.MainActivity
import com.xaillaclass.premium.R
import com.xaillaclass.premium.util.HotspotHelper
import com.xaillaclass.premium.util.RootHelper

class SettingsFragment : Fragment() {

    private val PREF_NAME = "xaillaclass_settings"
    private val KEY_THEME = "theme"
    private val KEY_ROOT = "root_mode"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val spinnerTheme = view.findViewById<Spinner>(R.id.spinnerTheme)
        val switchHotspot = view.findViewById<SwitchMaterial>(R.id.switchHotspot)
        val switchRoot = view.findViewById<SwitchMaterial>(R.id.switchRoot)
        
        val layoutInfoHotspot = view.findViewById<LinearLayout>(R.id.layoutInfoHotspot)
        val tvHotspotIp = view.findViewById<TextView>(R.id.tvHotspotIp)
        val tvHotspotPort = view.findViewById<TextView>(R.id.tvHotspotPort)
        val btnOpenHotspot = view.findViewById<Button>(R.id.btnOpenHotspotSettings)
        
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        tvVersion.text = "XaillaClass Premium v${BuildConfig.VERSION_NAME}"

        // --- TEMA ---
        val themes = arrayOf("Putih Elegan", "Biru Transparan", "Hitam Transparan")
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        spinnerTheme.adapter = themeAdapter
        spinnerTheme.setSelection(prefs.getInt(KEY_THEME, 0))

        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (prefs.getInt(KEY_THEME, 0) != position) {
                    prefs.edit().putInt(KEY_THEME, position).apply()
                    (requireActivity() as? MainActivity)?.applyTheme(position)
                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- ROOT PROXY ---
        switchRoot.isChecked = prefs.getBoolean(KEY_ROOT, false)
        switchRoot.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !RootHelper.isRooted()) {
                Toast.makeText(requireContext(), "HP tidak ter-root!", Toast.LENGTH_LONG).show()
                switchRoot.isChecked = false
                return@setOnCheckedChangeListener
            }
            prefs.edit().putBoolean(KEY_ROOT, isChecked).apply()
        }

        // --- TOMBOL BUKA HOTSPOT ---
        btnOpenHotspot.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Buka pengaturan Hotspot manual", Toast.LENGTH_SHORT).show()
            }
        }

        // --- HOTSPOT PROXY (VALIDASI) ---
        val isHotspotActuallyOn = HotspotHelper.isHotspotEnabled(requireContext())
        
        // Sinkronisasi status switch dengan kondisi asli
        switchHotspot.isChecked = prefs.getBoolean("proxy_hotspot_on", false) && isHotspotActuallyOn

        // Tampilkan info jika sudah nyala
        if (switchHotspot.isChecked) {
            layoutInfoHotspot.visibility = View.VISIBLE
            btnOpenHotspot.visibility = View.GONE
            tvHotspotIp.text = "IP: ${HotspotHelper.getHotspotIpAddress()}"
            tvHotspotPort.text = "Port: 10808"
        } else {
            layoutInfoHotspot.visibility = View.GONE
            btnOpenHotspot.visibility = View.VISIBLE
        }

        switchHotspot.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // CEK: Apakah Hotspot HP asli sudah menyala?
                if (!HotspotHelper.isHotspotEnabled(requireContext())) {
                    Toast.makeText(requireContext(), "Hotspot belum menyala. Nyalakan Hotspot HP terlebih dahulu!", Toast.LENGTH_LONG).show()
                    switchHotspot.isChecked = false // Tolak dan kembalikan switch
                    return@setOnCheckedChangeListener
                }
                
                // Jika sudah menyala, tampilkan info
                prefs.edit().putBoolean("proxy_hotspot_on", true).apply()
                layoutInfoHotspot.visibility = View.VISIBLE
                btnOpenHotspot.visibility = View.GONE
                tvHotspotIp.text = "IP: ${HotspotHelper.getHotspotIpAddress()}"
                tvHotspotPort.text = "Port: 10808"
                Toast.makeText(requireContext(), "Proxy Hotspot Aktif. Set Proxy di HP Client.", Toast.LENGTH_LONG).show()
                
            } else {
                // Saat dimatikan
                prefs.edit().putBoolean("proxy_hotspot_on", false).apply()
                layoutInfoHotspot.visibility = View.GONE
                btnOpenHotspot.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Proxy Hotspot dimatikan", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
