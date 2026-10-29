package com.xaillaclass.premium.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.xaillaclass.premium.BuildConfig
import com.xaillaclass.premium.R

class SettingsFragment : Fragment() {

    private val PREF_NAME = "xaillaclass_settings"
    private val KEY_THEME = "theme"
    private val KEY_HOTSPOT = "hotspot"
    private val KEY_ROOT = "root_mode"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        // Init Views
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val spinnerTheme = view.findViewById<Spinner>(R.id.spinnerTheme)
        val switchHotspot = view.findViewById<SwitchMaterial>(R.id.switchHotspot)
        val switchRoot = view.findViewById<SwitchMaterial>(R.id.switchRoot)
        
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Set Versi App
        tvVersion.text = "XaillaClass Premium v${BuildConfig.VERSION_NAME}"

        // --- FITUR TEMA ---
        val themes = arrayOf("Putih (Default)", "Hitam (Dark Mode)", "Biru")
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        spinnerTheme.adapter = themeAdapter
        
        // Load tema tersimpan
        val savedTheme = prefs.getInt(KEY_THEME, 0)
        spinnerTheme.setSelection(savedTheme)

        spinnerTheme.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, position: Int, id: Long) {
                prefs.edit().putInt(KEY_THEME, position).apply()
                when (position) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Putih
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // Hitam
                    2 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        Toast.makeText(requireContext(), "Tema Biru diterapkan", Toast.LENGTH_SHORT).show()
                        // Note: Untuk mengubah warna utama menjadi biru sepenuhnya, 
                        // kita perlu mengubah colorPrimary di themes.xml secara dinamis (via ThemeOverlay).
                    }
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // --- FITUR HOTSPOT ---
        switchHotspot.isChecked = prefs.getBoolean(KEY_HOTSPOT, false)
        switchHotspot.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_HOTSPOT, isChecked).apply()
            val status = if (isChecked) "Aktif" else "Mati"
            Toast.makeText(requireContext(), "Hotspot: $status", Toast.LENGTH_SHORT).show()
        }

        // --- FITUR ROOT PROXY ---
        switchRoot.isChecked = prefs.getBoolean(KEY_ROOT, false)
        switchRoot.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_ROOT, isChecked).apply()
            val mode = if (isChecked) "Root (iptables)" else "No Root (VpnService)"
            Toast.makeText(requireContext(), "Mode Proxy: $mode", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
