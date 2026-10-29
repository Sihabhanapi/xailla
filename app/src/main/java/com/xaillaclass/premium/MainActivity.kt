package com.xaillaclass.premium

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xaillaclass.premium.ui.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ambil Tema Sebelum Memuat Layout
        val prefs = getSharedPreferences("xaillaclass_settings", Context.MODE_PRIVATE)
        val theme = prefs.getInt("theme", 0)
        applyTheme(theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        if (savedInstanceState == null) loadFragment(HomeFragment())
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_config -> loadFragment(ConfigFragment())
                R.id.nav_log -> loadFragment(LogFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }

    fun applyTheme(theme: Int) {
        when (theme) {
            0 -> { // Putih
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.Theme_XaillaClass_White)
            }
            1 -> { // Biru
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.Theme_XaillaClass_Blue)
            }
            2 -> { // Hitam
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.Theme_XaillaClass_Black)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }
}
