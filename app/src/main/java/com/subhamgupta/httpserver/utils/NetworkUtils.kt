
package com.subhamgupta.httpserver.utils

import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIpAddress(): String? = getInetAddresses()
        .filter { it.isLocalAddress() }
        .map { it.hostAddress }
        .firstOrNull()

    private fun getInetAddresses() = NetworkInterface.getNetworkInterfaces()
        .iterator()
        .asSequence()
        .flatMap { networkInterface ->
            networkInterface.inetAddresses
                .asSequence()
                .filter { !it.isLoopbackAddress }
        }.toList()
}

fun InetAddress.isLocalAddress(): Boolean {
    try {
        return isSiteLocalAddress
                && !hostAddress!!.contains(":")
                && hostAddress != "127.0.0.1"
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}
fun getConnectedDevices(): List<InetAddress> {
    val devices = mutableListOf<InetAddress>()
    val interfaces = NetworkInterface.getNetworkInterfaces()

    while (interfaces.hasMoreElements()) {
        val networkInterface = interfaces.nextElement()
        val addresses = networkInterface.inetAddresses

        while (addresses.hasMoreElements()) {
            val address = addresses.nextElement()

            if (!address.isLoopbackAddress && address.isReachable(3000)) {
                devices.add(address)
            }
        }
    }

    return devices
}