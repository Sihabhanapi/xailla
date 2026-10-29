package com.xaillaclass.premium.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xaillaclass.premium.model.ServerConfig

object PrefsHelper {
    private const val PREF = "xaillaclass_pref"
    private const val KEY_CONFIGS = "configs"
    private const val KEY_SELECTED = "selected"
    private const val KEY_THEME = "theme"
    private const val KEY_VPN_ACTIVE = "vpn_active"

    fun saveConfigs(context: Context, configs: List<ServerConfig>) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_CONFIGS, Gson().toJson(configs)).apply()
    }

    fun loadConfigs(context: Context): MutableList<ServerConfig> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_CONFIGS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<ServerConfig>>() {}.type
        return Gson().fromJson(json, type) ?: mutableListOf()
    }

    fun setSelected(context: Context, id: String) = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_SELECTED, id).apply()
    fun getSelected(context: Context): ServerConfig? = loadConfigs(context).find { it.id == getSelectedId(context) }
    fun getSelectedId(context: Context): String? = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_SELECTED, null)

    fun setTheme(context: Context, theme: Int) = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt(KEY_THEME, theme).apply()
    fun getTheme(context: Context): Int = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(KEY_THEME, 0)

    fun setVpnActive(context: Context, active: Boolean) = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(KEY_VPN_ACTIVE, active).apply()
    fun isVpnActive(context: Context): Boolean = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY_VPN_ACTIVE, false)
}
