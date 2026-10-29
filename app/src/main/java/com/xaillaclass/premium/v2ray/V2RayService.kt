package com.xaillaclass.premium.v2ray

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

class V2RayService : VpnService() {

    private val channelId = "xaillaclass_vpn"
    private var coreController: CoreController? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    companion object {
        const val XRAY_PORT = 10808
        private const val TAG = "V2RayService"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "XaillaClass VPN", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        if (!isRunning) {
            startForeground(1, buildNotification())
            if (startXray() && establishVpn()) {
                isRunning = true
            } else {
                stopSelf()
            }
        }
        return START_STICKY
    }

    // 1. Jalankan Xray di background
    private fun startXray(): Boolean {
        return try {
            Libv2ray.initCoreEnv("", "")
            val callbackHandler = object : CoreCallbackHandler {
                override fun onEmitStatus(code: Long, message: String?): Long = 0
                override fun shutdown(): Long = 0
                override fun startup(): Long = 0
            }

            val configJson = """
            {
              "log": {"loglevel": "warning"},
              "inbounds": [
                {
                  "port": $XRAY_PORT,
                  "listen": "127.0.0.1",
                  "protocol": "socks",
                  "settings": {"auth": "noauth", "udp": true}
                }
              ],
              "outbounds": [{"protocol": "freedom"}]
            }
            """.trimIndent()

            coreController = Libv2ray.newCoreController(callbackHandler)
            coreController?.startLoop(configJson, XRAY_PORT)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Xray fail", e)
            false
        }
    }

    // 2. Bangun Antarmuka VPN (Memicu Ikon Kunci di Status Bar)
    private fun establishVpn(): Boolean {
        try {
            val builder = Builder()
                .setSession("XaillaClass")
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .setBlocking(true)

            vpnInterface = builder.establish()
            return vpnInterface != null
        } catch (e: Exception) {
            Log.e(TAG, "VPN Establish fail", e)
            return false
        }
    }

    private fun stopVpn() {
        try {
            coreController?.stopLoop()
            vpnInterface?.close()
        } catch (e: Exception) {}
        coreController = null
        vpnInterface = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("VPN Aktif & Terhubung")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
}
