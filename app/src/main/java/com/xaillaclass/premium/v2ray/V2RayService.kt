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
    private var tun2socksStarted = false

    companion object {
        const val XRAY_PORT = 10808
        private const val TAG = "V2RayService"
        private const val MTU = 1500
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
            if (startXray()) {
                if (establishVpn()) {
                    startTun2Socks()
                    isRunning = true
                } else {
                    Log.e(TAG, "establishVpn gagal")
                }
            } else {
                Log.e(TAG, "startXray gagal")
            }
            if (!isRunning) {
                stopVpn()
            }
        }
        return START_STICKY
    }

    // 1. Jalankan Xray SOCKS5 di 127.0.0.1:10808
    private fun startXray(): Boolean {
        return try {
            Libv2ray.initCoreEnv("", "")
            val handler = object : CoreCallbackHandler {
                override fun onEmitStatus(code: Long, message: String?): Long {
                    Log.d(TAG, "Xray[$code]: $message")
                    return 0
                }
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

            coreController = Libv2ray.newCoreController(handler)
            coreController?.startLoop(configJson, XRAY_PORT)
            Log.d(TAG, "Xray started di $XRAY_PORT")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Xray start fail", e)
            false
        }
    }

    // 2. Bikin antarmuka VPN (tun)
    private fun establishVpn(): Boolean {
        return try {
            val builder = Builder()
                .setSession("XaillaClass")
                .setMtu(MTU)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .setBlocking(true)

            vpnInterface = builder.establish()
            Log.d(TAG, "VPN interface = $vpnInterface")
            vpnInterface != null
        } catch (e: Throwable) {
            Log.e(TAG, "VPN establish fail", e)
            false
        }
    }

    // 3. Pasang tun2socks (REAL, bukan dummy)
    private fun startTun2Socks() {
        val iface = vpnInterface ?: return
        val fd = iface.fd
        val ok = Tun2SocksHelper.start(fd, "127.0.0.1", XRAY_PORT)
        tun2socksStarted = ok
        Log.d(TAG, "tun2socks started=$ok")
        if (!ok) {
            Log.e(TAG, "tun2socks gagal start, VPN tidak akan forward trafik")
        }
    }

    private fun stopVpn() {
        try {
            if (tun2socksStarted) {
                Tun2SocksHelper.stop()
                tun2socksStarted = false
            }
        } catch (e: Throwable) { }
        try { coreController?.stopLoop() } catch (e: Throwable) { }
        try { vpnInterface?.close() } catch (e: Throwable) { }
        coreController = null
        vpnInterface = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("VPN aktif & meneruskan trafik")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

    override fun onRevoke() { stopVpn(); super.onRevoke() }
    override fun onDestroy() { stopVpn(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
}
