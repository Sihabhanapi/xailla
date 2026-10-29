package com.xaillaclass.premium.v2ray

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.*
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class V2RayService : VpnService() {
    private val channelId = "xaillaclass_vpn"
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "XaillaClass VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("VPN Proxy Aktif (Port 8080)")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
        startForeground(1, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            startVpn()
            isRunning = true
        }
        return START_STICKY
    }

    private fun startVpn() {
        try {
            // Buat antarmuka VPN palsu (tun0) dengan IP lokal
            val builder = Builder()
                .setSession("XaillaClass")
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
            
            // Set proxy lokal di Port 8080 untuk ditangkap V2Ray nanti
            builder.setBlocking(true)
            vpnInterface = builder.establish()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {}
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
}
