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
import com.xaillaclass.premium.util.PrefsHelper
import com.xaillaclass.premium.util.RootHelper

class SettingsFragment : Fragment() {

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
        
        tvVersion.text = "XaillaClass Premium v${BuildConfig.VERSION_NAME}"

        // TEMA
        val themes = arrayOf("Putih Elegan", "Biru Transparan", "Hitam Transparan")
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        spinnerTheme.adapter = themeAdapter
        spinnerTheme.setSelection(PrefsHelper.getTheme(requireContext()))

        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (PrefsHelper.getTheme(requireContext()) != position) {
                    PrefsHelper.setTheme(requireContext(), position)
                    (requireActivity() as? MainActivity)?.applyTheme(position)
                    requireActivity().recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ROOT
        switchRoot.isChecked = RootHelper.isRooted() // Cek status root
        switchRoot.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !RootHelper.isRooted()) {
                Toast.makeText(requireContext(), "HP tidak ter-root!", Toast.LENGTH_LONG).show()
                switchRoot.isChecked = false
            }
        }

        // HOTSPOT
        btnOpenHotspot.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Buka pengaturan Hotspot manual", Toast.LENGTH_SHORT).show()
            }
        }

        // Sinkronisasi status Hotspot
        if (HotspotHelper.isHotspotEnabled(requireContext())) {
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
                if (!HotspotHelper.isHotspotEnabled(requireContext())) {
                    Toast.makeText(requireContext(), "Hotspot belum menyala. Nyalakan manual!", Toast.LENGTH_LONG).show()
                    switchHotspot.isChecked = false
                    return@setOnCheckedChangeListener
                }
                val success = HotspotHelper.setHotspotEnabled(requireContext(), true, switchRoot.isChecked)
                if (success) {
                    layoutInfoHotspot.visibility = View.VISIBLE
                    btnOpenHotspot.visibility = View.GONE
                    tvHotspotIp.text = "IP: ${HotspotHelper.getHotspotIpAddress()}"
                    tvHotspotPort.text = "Port: 10808"
                } else {
                    switchHotspot.isChecked = false
                }
            } else {
                HotspotHelper.setHotspotEnabled(requireContext(), false, switchRoot.isChecked)
                layoutInfoHotspot.visibility = View.GONE
                btnOpenHotspot.visibility = View.VISIBLE
            }
        }

        return view
    }
}
