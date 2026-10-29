package com.xaillaclass.premium.util
import java.io.DataOutputStream

object RootHelper {
    fun isRooted(): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        p.inputStream.bufferedReader().readText().contains("uid=0")
    } catch (e: Exception) { false }

    fun execCmd(cmd: String): Boolean = try {
        val p = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(p.outputStream)
        os.writeBytes("$cmd\n")
        os.writeBytes("exit\n")
        os.flush()
        p.waitFor() == 0
    } catch (e: Exception) { false }
}
