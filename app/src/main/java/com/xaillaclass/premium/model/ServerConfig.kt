package com.xaillaclass.premium.model

data class ServerConfig(
    val id: String, 
    val name: String, 
    val protocol: String, 
    val address: String, 
    val port: Int,
    val uuid: String = "", 
    val password: String = "", 
    val alterId: String = "",
    val security: String = "auto",
    val network: String = "tcp",
    val headType: String = "",
    val wsHost: String = "",
    val wsPath: String = "",
    val finalMask: String = "",
    val tls: Boolean = false,
    val sni: String = "", 
    val fingerprint: String = "",
    val alpn: String = "",
    val allowInsecure: Boolean = false,
    val echConfigList: String = "",
    val verifyPeerCertByName: String = "",
    val certFingerprint: String = "",
    val fragment: Boolean = false,
    val sniSpoofing: Boolean = false
)
