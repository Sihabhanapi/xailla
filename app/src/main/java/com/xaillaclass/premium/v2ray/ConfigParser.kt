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

    private fun safeDecode(input: String): String = try {
        String(Base64.decode(input, Base64.NO_WRAP))
    } catch (e: Exception) { "" }

    private fun parseVmess(link: String): ServerConfig? {
        val json = safeDecode(link.removePrefix("vmess://").trim())
        if (json.isEmpty()) return null
        val obj = JsonParser.parseString(json).asJsonObject
        return ServerConfig(
            id = System.currentTimeMillis().toString(),
            name = obj.get("ps")?.asString ?: "VMess",
            protocol = "vmess",
            address = obj.get("add")?.asString ?: "",
            port = obj.get("port")?.asString?.toIntOrNull() ?: 443,
            uuid = obj.get("id")?.asString ?: "",
            network = obj.get("net")?.asString ?: "tcp",
            tls = obj.get("tls")?.asString == "tls",
            sni = obj.get("sni")?.asString ?: obj.get("host")?.asString ?: ""
        )
    }

    private fun parseVless(link: String): ServerConfig? {
        val uri = URI(link)
        val query = uri.query ?: ""
        val params = query.split("&").associate {
            val p = it.split("=", limit = 2)
            p[0] to p.getOrElse(1) { "" }
        }
        return ServerConfig(
            id = System.currentTimeMillis().toString(),
            name = uri.fragment ?: "VLESS",
            protocol = "vless",
            address = uri.host ?: "",
            port = if (uri.port == -1) 443 else uri.port,
            uuid = uri.userInfo ?: "",
            network = params["type"] ?: "tcp",
            tls = params["security"] == "tls" || params["security"] == "reality",
            sni = params["sni"] ?: params["host"] ?: ""
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
            tls = true,
            sni = uri.host ?: ""
        )
    }

    private fun parseShadowsocks(link: String): ServerConfig? {
        val body = link.removePrefix("ss://").substringBefore("#")
        val decoded = safeDecode(body)
        if (!decoded.contains("@")) return null
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
