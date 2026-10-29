package com.xaillaclass.premium.util

import java.security.MessageDigest
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    private const val SECRET_KEY = "XaillaClassPremium@2026#SecretKey"

    private fun getKey(): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val key = sha.digest(SECRET_KEY.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(Arrays.copyOf(key, 32), "AES")
    }

    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            encrypted.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) { "" }
    }

    fun decrypt(hexData: String): String {
        return try {
            val bytes = ByteArray(hexData.length / 2)
            for (i in bytes.indices) {
                val index = i * 2
                bytes[i] = hexData.substring(index, index + 2).toInt(16).toByte()
            }
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getKey())
            String(cipher.doFinal(bytes), Charsets.UTF_8)
        } catch (e: Exception) { "" }
    }
}
