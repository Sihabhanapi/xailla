package com.xaillaclass.premium.util

import java.io.DataOutputStream

object RootHelper {

    fun isRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    fun execRootCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isV2RayEngineAvailable(): Boolean {
        val paths = listOf(
            "/data/data/com.v2ray.ang",
            "/data/data/io.nekohasekai.sagernet",
            "/data/data/com.github.kr328.clash"
        )
        return paths.any { java.io.File(it).exists() }
    }
}
