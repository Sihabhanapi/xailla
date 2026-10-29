package com.xaillaclass.premium.v2ray

import android.util.Log

/**
 * JNI Wrapper untuk hev-socks5-tunnel.
 * Memakai binary .so yang sudah include fungsi tun2socks.
 */
object Tun2SocksHelper {

    private const val TAG = "Tun2Socks"
    private var loaded = false

    fun load(): Boolean {
        if (loaded) return true
        return try {
            System.loadLibrary("tun2socks")
            loaded = true
            Log.d(TAG, "hev-socks5-tunnel loaded OK")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Gagal load libtun2socks.so", e)
            false
        }
    }

    // Fungsi JNI yang di-export oleh hev-socks5-tunnel
    // (signature dari repo heiher/hev-socks5-tunnel)
    external fun TProxyStartService(configPath: String, fd: Int): Int
    external fun TProxyStopService(): Int
    external fun TProxyGetStats(): LongArray?

    /**
     * Mulai tunnel. tunFd harus dari VpnService.establish().
     */
    fun start(tunFd: Int, socksHost: String = "127.0.0.1", socksPort: Int = 10808): Boolean {
        if (!load()) return false
        return try {
            // Buat file konfigurasi YAML minimal
            val configYml = """
            tunnel:
              mtu: 1500
            socks5:
              port: $socksPort
              address: $socksHost
              udp: 'udp'
            misc:
              task-stack-size: 20480
              connect-timeout: 5000
              read-write-timeout: 60000
              log-level: warn
            """.trimIndent()

            val configFile = java.io.File("/data/local/tmp/hev-t2s.yml")
            try {
                configFile.parentFile?.mkdirs()
                configFile.writeText(configYml)
            } catch (e: Exception) {
                // Fallback ke cache dir kalau /data/local/tmp tidak writable
                val cacheDir = java.io.File(System.getProperty("java.io.tmpdir") ?: "/tmp")
                cacheDir.mkdirs()
                val fallback = java.io.File(cacheDir, "hev-t2s.yml")
                fallback.writeText(configYml)
                return TProxyStartService(fallback.absolutePath, tunFd) == 0
            }

            val rc = TProxyStartService(configFile.absolutePath, tunFd)
            Log.d(TAG, "TProxyStartService returned $rc")
            rc == 0
        } catch (e: Throwable) {
            Log.e(TAG, "start() failed", e)
            false
        }
    }

    fun stop() {
        try {
            if (loaded) TProxyStopService()
        } catch (e: Throwable) {
            Log.e(TAG, "stop() failed", e)
        }
    }
}
