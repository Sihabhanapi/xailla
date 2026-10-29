package com.xaillaclass.premium.ui

import android.content.Context
import android.os.Bundle
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
            if (isChecked) {
                if (!RootHelper.isRooted()) {
                    Toast.makeText(requireContext(), "HP Tidak ter-root / Izin ditolak!", Toast.LENGTH_LONG).show()
                    switchRoot.isChecked = false 
                    return@setOnCheckedChangeListener
                }
            }
            prefs.edit().putBoolean(KEY_ROOT, isChecked).apply()
        }

        // --- HOTSPOT PROXY ---
        switchHotspot.isChecked = HotspotHelper.isHotspotEnabled(requireContext())
        if (switchHotspot.isChecked) layoutInfoHotspot.visibility = View.VISIBLE

        switchHotspot.setOnCheckedChangeListener { _, isChecked ->
            val useRoot = switchRoot.isChecked
            val success = HotspotHelper.setHotspotEnabled(requireContext(), isChecked, useRoot)
            
            if (success) {
                if (isChecked) {
                    layoutInfoHotspot.visibility = View.VISIBLE
                    val ip = HotspotHelper.getHotspotIpAddress()
                    tvHotspotIp.text = "IP: $ip"
                    tvHotspotPort.text = "Port: 8080"
                    
                    if (useRoot) {
                        Toast.makeText(requireContext(), "Mode Root: iptables VPN Gateway Aktif", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Mode No-Root: Proxy Hotspot aktif", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    layoutInfoHotspot.visibility = View.GONE
                    Toast.makeText(requireContext(), "Hotspot dimatikan", Toast.LENGTH_SHORT).show()
                }
            } else {
                switchHotspot.isChecked = !isChecked
            }
        }

        return view
    }
}
