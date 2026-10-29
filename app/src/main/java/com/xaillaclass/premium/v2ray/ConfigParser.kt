package com.xaillaclass.premium.v2ray
import android.util.Base64
import com.google.gson.JsonParser
import com.xaillaclass.premium.model.ServerConfig
import java.nio.charset.StandardCharsets
object ConfigParser {
    fun parseLink(link: String): ServerConfig? = try {
        when {
            link.startsWith("vmess://") -> parseVmess(link)
            link.startsWith("vless://") -> parseVless(link)
            link.startsWith("trojan://") -> parseTrojan(link)
            link.startsWith("ss://") -> parseShadowsocks(link)
            else -> null
        }
    } catch (e: Exception) { null }
    private fun safeDecode(input: String): String = try {
        String(Base64.decode(input, Base64.DEFAULT), StandardCharsets.UTF_8)
    } catch (e: Exception) { "" }
    private fun parseVmess(link: String): ServerConfig? {
        val json = safeDecode(link.removePrefix("vmess://").trim())
        if (json.isEmpty()) return null
        val obj = JsonParser.parseString(json).asJsonObject
        return ServerConfig(
            id = System.currentTimeMillis().toString(), name = obj.get("ps")?.asString ?: "VMess",
            protocol = "vmess", address = obj.get("add")?.asString ?: "",
            port = obj.get("port")?.asString?.toIntOrNull() ?: 443, uuid = obj.get("id")?.asString ?: "",
            network = obj.get("net")?.asString ?: "tcp", tls = obj.get("tls")?.asString == "tls"
        )
    }
    private fun parseVless(link: String): ServerConfig? {
        val uri = java.net.URI(link)
        val params = uri.query?.split("&")?.associate { val p = it.split("=", limit = 2); p[0] to p.getOrElse(1) { "" } } ?: emptyMap()
        return ServerConfig(
            id = System.currentTimeMillis().toString(), name = uri.fragment ?: "VLESS",
            protocol = "vless", address = uri.host ?: "", port = if (uri.port == -1) 443 else uri.port,
            uuid = uri.userInfo ?: "", network = params["type"] ?: "tcp", tls = params["security"] == "tls"
        )
    }
    private fun parseTrojan(link: String): ServerConfig? {
        val uri = java.net.URI(link)
        return ServerConfig(
            id = System.currentTimeMillis().toString(), name = uri.fragment ?: "Trojan",
            protocol = "trojan", address = uri.host ?: "", port = if (uri.port == -1) 443 else uri.port,
            password = uri.userInfo ?: "", tls = true
        )
    }
    private fun parseShadowsocks(link: String): ServerConfig? {
        val decoded = safeDecode(link.removePrefix("ss://").substringBefore("#"))
        if (decoded.isEmpty() || !decoded.contains("@")) return null
        val (methodPass, hostPort) = decoded.split("@", limit = 2)
        val (host, port) = hostPort.split(":")
        return ServerConfig(
            id = System.currentTimeMillis().toString(), name = "Shadowsocks",
            protocol = "shadowsocks", address = host, port = port.toIntOrNull() ?: 443, password = methodPass
        )
    }
}
