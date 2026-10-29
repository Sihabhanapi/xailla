package com.xaillaclass.premium.model
data class ServerConfig(
    val id: String, val name: String, val protocol: String, val address: String, val port: Int,
    val uuid: String = "", val password: String = "", val network: String = "tcp",
    val tls: Boolean = false, val sni: String = "", val path: String = ""
)
