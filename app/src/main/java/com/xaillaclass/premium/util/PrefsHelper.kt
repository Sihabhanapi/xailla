package com.xaillaclass.premium.util
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xaillaclass.premium.model.ServerConfig
object PrefsHelper {
    private const val PREF = "xaillaclass_pref"
    private const val KEY_CONFIGS = "configs"
    private const val KEY_SELECTED = "selected"
    fun saveConfigs(context: Context, configs: List<ServerConfig>) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_CONFIGS, Gson().toJson(configs)).apply()
    }
    fun loadConfigs(context: Context): MutableList<ServerConfig> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_CONFIGS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<ServerConfig>>() {}.type
        return Gson().fromJson(json, type) ?: mutableListOf()
    }
    fun setSelected(context: Context, id: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_SELECTED, id).apply()
    }
}
