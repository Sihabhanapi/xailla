package com.xaillaclass.premium.v2ray

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import java.io.File

class V2RayService : Service() {

    private val channelId = "xaillaclass_vpn"
    private var coreController: CoreController? = null
    private var isRunning = false

    companion object {
        const val XRAY_PORT = 10808  // SOCKS port untuk redirect iptables
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
            stopXray()
            return START_NOT_STICKY
        }
        if (!isRunning) {
            startForeground(1, buildNotification())
            if (startXray()) {
                isRunning = true
            } else {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startXray(): Boolean {
        return try {
            // 1. Init Core Environment (geoip & geosite)
            val filesDir = applicationContext.filesDir
            val geoipFile = File(filesDir, "geoip.dat")
            val geositeFile = File(filesDir, "geosite.dat")
            
            // Copy asset dari AAR ke filesDir (jika belum ada)
            copyAssetIfNotExist("geoip.dat", geoipFile)
            copyAssetIfNotExist("geosite.dat", geositeFile)
            
            Libv2ray.initCoreEnv(geoipFile.absolutePath, geositeFile.absolutePath)
            Log.d(TAG, "Xray version: ${Libv2ray.checkVersionX()}")
            
            // 2. Buat callback handler
            val callbackHandler = object : CoreCallbackHandler {
                override fun onEmitStatus(code: Long, message: String?): Long {
                    Log.d(TAG, "Xray status [$code]: $message")
                    return 0
                }
                override fun shutdown(): Long {
                    Log.d(TAG, "Xray shutdown")
                    return 0
                }
                override fun startup(): Long {
                    Log.d(TAG, "Xray startup")
                    return 0
                }
            }
            
            // 3. Buat config JSON (contoh: SOCKS5 + freedom outbound)
            // GANTI dengan config server VMess/VLESS milik kamu
            val configJson = """
            {
              "log": {"loglevel": "warning"},
              "inbounds": [
                {
                  "port": $XRAY_PORT,
                  "listen": "0.0.0.0",
                  "protocol": "socks",
                  "settings": {"auth": "noauth", "udp": true}
                },
                {
                  "port": 10809,
                  "listen": "0.0.0.0",
                  "protocol": "http"
                }
              ],
              "outbounds": [
                {"protocol": "freedom", "tag": "direct"}
              ]
            }
            """.trimIndent()
            
            // 4. Jalankan Xray
            coreController = Libv2ray.newCoreController(callbackHandler)
            coreController?.startLoop(configJson, XRAY_PORT)
            Log.d(TAG, "Xray started on port $XRAY_PORT")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Xray", e)
            false
        }
    }

    private fun stopXray() {
        try {
            coreController?.stopLoop()
            coreController = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop Xray", e)
        }
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun copyAssetIfNotExist(assetName: String, target: File) {
        if (target.exists()) return
        try {
            assets.open(assetName).use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Copy asset failed: $assetName", e)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("Xray core aktif di port $XRAY_PORT")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        stopXray()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
