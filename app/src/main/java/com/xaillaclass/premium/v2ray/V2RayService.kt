package com.xaillaclass.premium.v2ray

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat

class V2RayService : Service() {
    private val channelId = "xaillaclass_vpn"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "XaillaClass VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("XaillaClass Premium")
            .setContentText("Terhubung...")
            .setSmallIcon(android.R.drawable.ic_lock_lock) // FIX: Menggunakan ikon standar Android
            .setOngoing(true)
            .build()
        startForeground(1, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
