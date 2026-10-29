package com.xaillaclass.premium.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.io.DataOutputStream
import java.lang.reflect.Method
import java.net.NetworkInterface

object HotspotHelper {

    // Mendapatkan IP Address Hotspot (biasanya 192.168.43.1)
    fun getHotspotIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.name.contains("wlan") || networkInterface.name.contains("ap")) {
                    val addresses = networkInterface.inetAddresses
                    for (address in addresses) {
                        if (!address.isLoopbackAddress && address.hostAddress?.contains(":") == false) {
                            return address.hostAddress ?: "192.168.43.1"
                        }
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return "192.168.43.1" // Default Android Hotspot IP
    }

    fun setHotspotEnabled(context: Context, enabled: Boolean, useRoot: Boolean): Boolean {
        return if (useRoot) {
            // --- LOGIKA ROOT ---
            if (!RootHelper.isRooted()) {
                Toast.makeText(context, "Akses Root ditolak / Tidak tersedia!", Toast.LENGTH_LONG).show()
                return false
            }
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                // Perintah iptables untuk redirect trafik hotspot ke proxy lokal (contoh port 8080)
                if (enabled) {
                    os.writeBytes("sysctl -w net.ipv4.ip_forward=1\n")
                    os.writeBytes("iptables -t nat -A PREROUTING -i wlan0 -p tcp --dport 80 -j REDIRECT --to-port 8080\n")
                    os.writeBytes("iptables -t nat -A PREROUTING -i wlan0 -p tcp --dport 443 -j REDIRECT --to-port 8080\n")
                } else {
                    os.writeBytes("iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 80 -j REDIRECT --to-port 8080\n")
                    os.writeBytes("iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 443 -j REDIRECT --to-port 8080\n")
                }
                os.writeBytes("exit\n")
                os.flush()
                os.close()
                process.waitFor()
                true
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menjalankan perintah Root", Toast.LENGTH_SHORT).show()
                false
            }
        } else {
            // --- LOGIKA NO-ROOT ---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return false
            }
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            try {
                val method: Method = wifiManager.javaClass.getMethod(
                    "setWifiApEnabled", android.net.wifi.WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
                )
                wifiManager.isWifiEnabled = false
                method.invoke(wifiManager, null, enabled) as Boolean
            } catch (e: Exception) {
                false
            }
        }
    }
}
