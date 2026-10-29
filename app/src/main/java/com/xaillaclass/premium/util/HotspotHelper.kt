package com.xaillaclass.premium.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.net.NetworkInterface

object HotspotHelper {

    private const val XRAY_PORT = 10808

    fun getHotspotIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (ni in interfaces) {
                val name = ni.name ?: continue
                if (name.startsWith("ap") || name.startsWith("swlan") || name.contains("wlan")) {
                    for (addr in ni.inetAddresses) {
                        if (!addr.isLoopbackAddress && addr.hostAddress?.contains(":") == false) {
                            return addr.hostAddress ?: "192.168.43.1"
                        }
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return "192.168.43.1"
    }

    fun isHotspotEnabled(context: Context): Boolean {
        return try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = wifi.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifi) as Boolean
        } catch (e: Exception) { false }
    }

    fun setHotspotEnabled(context: Context, enabled: Boolean, useRoot: Boolean): Boolean {
        return if (useRoot) {
            enableHotspotRoot(context, enabled)
        } else {
            enableHotspotNoRoot(context, enabled)
        }
    }

    private fun enableHotspotRoot(context: Context, enabled: Boolean): Boolean {
        if (!RootHelper.isRooted()) {
            Toast.makeText(context, "Root tidak tersedia", Toast.LENGTH_LONG).show()
            return false
        }

        return try {
            val result = if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    RootHelper.execRootCommand("cmd connectivity tether start") ||
                    RootHelper.execRootCommand("svc wifi enable; settings put global tether_enabled 1")
                } else {
                    RootHelper.execRootCommand("svc wifi enable") &&
                    RootHelper.execRootCommand("settings put global tether_enabled 1")
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    RootHelper.execRootCommand("cmd connectivity tether stop")
                } else {
                    RootHelper.execRootCommand("settings put global tether_enabled 0")
                }
            }

            if (result) {
                if (enabled) applyProxyIptables() else removeProxyIptables()
                true
            } else {
                Toast.makeText(context, "Gagal tether via root", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun enableHotspotNoRoot(context: Context, enabled: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            try {
                val intent = android.content.Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + context.packageName)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Toast.makeText(context, "Izinkan pengaturan sistem, lalu coba lagi", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {}
            return false
        }

        return try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = wifi.javaClass.getMethod(
                "setWifiApEnabled", android.net.wifi.WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
            )
            wifi.isWifiEnabled = false
            method.invoke(wifi, null, enabled) as Boolean
        } catch (e: Exception) {
            Toast.makeText(context, "HP tidak support auto-hotspot. Nyalakan manual.", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun applyProxyIptables() {
        // Script VPN Gateway dengan deteksi interface otomatis
        val script = """
            echo 1 > /proc/sys/net/ipv4/ip_forward
            IFACE=${'$'}(ip link show | grep -oE 'ap[0-9]+|swlan[0-9]+|wlan[0-9]+' | head -n 1)
            if [ -z "${'$'}IFACE" ]; then IFACE=wlan0; fi
            iptables -P FORWARD ACCEPT
            iptables -t nat -A POSTROUTING -o ${'$'}IFACE -j MASQUERADE
            iptables -t nat -A PREROUTING -i ${'$'}IFACE -p tcp --dport 80 -j REDIRECT --to-port $XRAY_PORT
            iptables -t nat -A PREROUTING -i ${'$'}IFACE -p tcp --dport 443 -j REDIRECT --to-port $XRAY_PORT
        """.trimIndent()
        
        RootHelper.execRootCommand(script)
    }

    private fun removeProxyIptables() {
        RootHelper.execRootCommand(
            """
            iptables -t nat -D POSTROUTING -o wlan0 -j MASQUERADE 2>/dev/null
            iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 80 -j REDIRECT --to-port $XRAY_PORT 2>/dev/null
            iptables -t nat -D PREROUTING -i wlan0 -p tcp --dport 443 -j REDIRECT --to-port $XRAY_PORT 2>/dev/null
            """.trimIndent()
        )
    }
}
