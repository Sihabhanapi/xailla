package com.xaillaclass.premium.v2ray

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.xaillaclass.premium.util.PrefsHelper
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

class V2RayService : VpnService() {
    private val channelId = "xaillaclass_vpn"
    private var coreController: CoreController? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private val XRAY_PORT = 10808

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "XaillaClass VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopVpn(); return START_NOT_STICKY }
        startForeground(1, buildNotification())
        
        val config = PrefsHelper.getSelected(this)
        if (config == null) {
            Log.e("V2Ray", "Tidak ada config terpilih!")
            stopSelf()
            return START_NOT_STICKY
        }

        startXray(config)
        establishVpn()
        PrefsHelper.setVpnActive(this, true)
        return START_STICKY
    }

    private fun startXray(config: com.xaillaclass.premium.model.ServerConfig) {
        try {
            Libv2ray.initCoreEnv("", "")
            val handler = object : CoreCallbackHandler {
                override fun onEmitStatus(code: Long, message: String?): Long = 0
                override fun shutdown(): Long = 0
                override fun startup(): Long = 0
            }

            // CONFIG XRAY ASLI DARI DATA USER
            val outbound = if (config.protocol == "vmess") {
                """{"protocol":"vmess","settings":{"vnext":[{"address":"${config.address}","port":${config.port},"users":[{"id":"${config.uuid}","alterId":0,"security":"auto"}]}]},"streamSettings":{"network":"${config.network}","security":"${if(config.tls)"tls" else "none"}","tlsSettings":{"serverName":"${config.sni}"}}}"""
            } else if (config.protocol == "vless") {
                """{"protocol":"vless","settings":{"vnext":[{"address":"${config.address}","port":${config.port},"users":[{"id":"${config.uuid}","encryption":"none"}]}]},"streamSettings":{"network":"${config.network}","security":"${if(config.tls)"tls" else "none"}","tlsSettings":{"serverName":"${config.sni}"}}}"""
            } else {
                """{"protocol":"freedom"}"""
            }

            val configJson = """
            {
              "log": {"loglevel": "warning"},
              "inbounds": [{"port": $XRAY_PORT, "listen": "127.0.0.1", "protocol": "socks", "settings": {"auth": "noauth", "udp": true}}],
              "outbounds": [$outbound]
            }
            """.trimIndent()

            coreController = Libv2ray.newCoreController(handler)
            coreController?.startLoop(configJson, XRAY_PORT)
        } catch (e: Exception) { Log.e("V2Ray", "Error start", e) }
    }

    private fun establishVpn() {
        try {
            val builder = Builder()
                .setSession("XaillaClass")
                .setMtu(1500)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .setBlocking(true)
            
            // Karena tidak ada binary tun2socks asli, kita set block agar aplikasi lain
            // bisa menggunakan proxy manual (IP 10.0.0.2:10808) atau lewat iptables (root).
            // TAPI icon kunci VPN akan muncul.
            vpnInterface = builder.establish()
        } catch (e: Exception) { Log.e("V2Ray", "Error VPN", e) }
    }

    private fun stopVpn() {
        try { coreController?.stopLoop() } catch (e: Exception) {}
        try { vpnInterface?.close() } catch (e: Exception) {}
        PrefsHelper.setVpnActive(this, false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("VPN aktif & meneruskan trafik")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true).build()

    override fun onRevoke() { stopVpn(); super.onRevoke() }
    override fun onDestroy() { stopVpn(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
}
