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

    fun getHotspotIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                // Deteksi interface hotspot (bisa wlan0, ap0, swlan0)
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
        return "192.168.43.1"
    }

    fun setHotspotEnabled(context: Context, enabled: Boolean, useRoot: Boolean): Boolean {
        return if (useRoot) {
            if (!RootHelper.isRooted()) {
                Toast.makeText(context, "Akses Root ditolak / Tidak tersedia!", Toast.LENGTH_LONG).show()
                return false
            }
            
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                
                if (enabled) {
                    // Nyalakan Hotspot
                    enableNativeHotspot(context)
                    
                    // --- SCRIPT VPN GATEWAY DIPERBAIKI (Otomatis deteksi interface) ---
                    // 1. Aktifkan IP Forwarding
                    os.writeBytes("echo 1 > /proc/sys/net/ipv4/ip_forward\n")
                    
                    // 2. Cari nama interface hotspot secara dinamis
                    os.writeBytes("IFACE=\$(ip link show | grep -o 'ap[0-9]*' | head -n 1)\n")
                    os.writeBytes("if [ -z \"\$IFACE\" ]; then IFACE=\"wlan0\"; fi\n")
                    
                    // 3. Terapkan NAT Routing ke Tun0 (VPN) & Interface Hotspot
                    os.writeBytes("iptables -P FORWARD ACCEPT\n")
                    os.writeBytes("iptables -t nat -A POSTROUTING -o tun0 -j MASQUERADE\n")
                    os.writeBytes("iptables -t nat -A POSTROUTING -o \$IFACE -j MASQUERADE\n")
                    
                    // 4. Redirect port 80/443 ke port 8080 (Local Proxy V2Ray)
                    os.writeBytes("iptables -t nat -A PREROUTING -i \$IFACE -p tcp --dport 80 -j REDIRECT --to-port 8080\n")
                    os.writeBytes("iptables -t nat -A PREROUTING -i \$IFACE -p tcp --dport 443 -j REDIRECT --to-port 8080\n")
                } else {
                    disableNativeHotspot(context)
                    // Hapus aturan iptables (Bersihkan)
                    os.writeBytes("iptables -t nat -D POSTROUTING -o tun0 -j MASQUERADE\n")
                    os.writeBytes("iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 80 -j REDIRECT --to-port 8080\n")
                    os.writeBytes("iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 443 -j REDIRECT --to-port 8080\n")
                }
                
                os.writeBytes("exit\n")
                os.flush()
                os.close()
                process.waitFor()
                return true
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menjalankan script Root: ${e.message}", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return false
            }
            return enableNativeHotspot(context)
        }
    }

    private fun enableNativeHotspot(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method: Method = wifiManager.javaClass.getMethod(
                "setWifiApEnabled", android.net.wifi.WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
            )
            wifiManager.isWifiEnabled = false
            method.invoke(wifiManager, null, true) as Boolean
        } catch (e: Exception) { false }
    }

    private fun disableNativeHotspot(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method: Method = wifiManager.javaClass.getMethod(
                "setWifiApEnabled", android.net.wifi.WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
            )
            method.invoke(wifiManager, null, false) as Boolean
        } catch (e: Exception) { false }
    }

    fun isHotspotEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method: Method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) { false }
    }
}
