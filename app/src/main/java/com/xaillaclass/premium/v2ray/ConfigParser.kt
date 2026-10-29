package com.xaillaclass.premium.v2ray

import android.util.Base64
import com.google.gson.JsonParser
import com.xaillaclass.premium.model.ServerConfig
import java.net.URI

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

    private fun parseVmess(link: String): ServerConfig? {
        val b64 = link.removePrefix("vmess://").trim()
        val json = String(Base64.decode(b64, Base64.NO_WRAP))
        val obj = JsonParser.parseString(json).asJsonObject
        
        return ServerConfig(
            id = System.currentTimeMillis().toString(), 
            name = obj.get("ps")?.asString ?: "VMess",
            protocol = "vmess", 
            address = obj.get("add")?.asString ?: "", 
            port = obj.get("port")?.asString?.toIntOrNull() ?: 443,
            uuid = obj.get("id")?.asString ?: "", 
            alterId = obj.get("aid")?.asString ?: "0",
            security = obj.get("scy")?.asString ?: "auto",
            network = obj.get("net")?.asString ?: "tcp",
            headType = obj.get("type")?.asString ?: "",
            wsHost = obj.get("host")?.asString ?: "",
            wsPath = obj.get("path")?.asString ?: "",
            tls = obj.get("tls")?.asString == "tls",
            sni = obj.get("sni")?.asString ?: "",
            fingerprint = obj.get("fp")?.asString ?: "",
            alpn = obj.get("alpn")?.asString ?: "",
            allowInsecure = false
        )
    }

    private fun parseVless(link: String): ServerConfig? {
        val uri = URI(link)
        val query = uri.query?.split("&")?.associate { it.split("=").let { p -> p[0] to p.getOrElse(1){""} } } ?: emptyMap()
        return ServerConfig(
            id = System.currentTimeMillis().toString(), 
            name = uri.fragment ?: "VLESS",
            protocol = "vless", 
            address = uri.host ?: "", 
            port = if (uri.port == -1) 443 else uri.port,
            uuid = uri.userInfo ?: "", 
            network = query["type"] ?: "tcp", 
            tls = query["security"] == "tls",
            sni = query["sni"] ?: "",
            wsPath = query["path"] ?: "",
            wsHost = query["host"] ?: "",
            fingerprint = query["fp"] ?: "",
            alpn = query["alpn"] ?: ""
        )
    }

    private fun parseTrojan(link: String): ServerConfig? {
        val uri = URI(link)
        return ServerConfig(
            id = System.currentTimeMillis().toString(), 
            name = uri.fragment ?: "Trojan",
            protocol = "trojan", 
            address = uri.host ?: "", 
            port = if (uri.port == -1) 443 else uri.port,
            password = uri.userInfo ?: "", 
            tls = true
        )
    }

    private fun parseShadowsocks(link: String): ServerConfig? {
        val body = link.removePrefix("ss://").substringBefore("#")
        val decoded = String(Base64.decode(body, Base64.NO_WRAP))
        val (methodPass, hostPort) = decoded.split("@", limit = 2)
        val (host, port) = hostPort.split(":")
        return ServerConfig(
            id = System.currentTimeMillis().toString(), 
            name = "Shadowsocks",
            protocol = "shadowsocks", 
            address = host, 
            port = port.toIntOrNull() ?: 443, 
            password = methodPass
        )
    }
}
