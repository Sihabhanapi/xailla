package com.xaillaclass.premium.util

import java.io.DataOutputStream

object RootHelper {
    fun isRooted(): Boolean {
        var rooted = false
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor()
            rooted = process.exitValue() == 0
        } catch (e: Exception) {
            rooted = false
        }
        return rooted
    }
}
