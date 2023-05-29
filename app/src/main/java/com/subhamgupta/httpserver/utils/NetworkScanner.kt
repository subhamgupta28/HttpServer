package com.subhamgupta.httpserver.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface

class NetworkScanner(private val context: Context) {
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun scanNetwork(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val ipByteArray = byteArrayOf(
            (ipAddress and 0xFF).toByte(),
            (ipAddress shr 8 and 0xFF).toByte(),
            (ipAddress shr 16 and 0xFF).toByte(),
            (ipAddress shr 24 and 0xFF).toByte()
        )

        val subnet = getSubnet(ipByteArray)
        val connectedDevices = getConnectedDevices(subnet)

        // Process the list of connected devices
        for (device in connectedDevices) {
            val ipAddress = device.hostAddress
            val macAddress = getMacAddress(device)

            // Log or store the device information as per your requirements
            println("Device found - IP: $ipAddress, MAC: $macAddress")
            map[macAddress] = ipAddress
        }
        return map
    }

    private fun getSubnet(ip: ByteArray): String {
        val subnetMask = wifiManager.dhcpInfo.netmask
        val subnetMaskByteArray = byteArrayOf(
            (subnetMask and 0xFF).toByte(),
            (subnetMask shr 8 and 0xFF).toByte(),
            (subnetMask shr 16 and 0xFF).toByte(),
            (subnetMask shr 24 and 0xFF).toByte()
        )

        val subnetByteArray = ByteArray(4)

        for (i in 0..3) {
            subnetByteArray[i] = (ip[i].toInt() and subnetMaskByteArray[i].toInt()).toByte()
        }

        return InetAddress.getByAddress(subnetByteArray).hostAddress
    }

    private fun getConnectedDevices(subnet: String): List<InetAddress> {
        val connectedDevices = mutableListOf<InetAddress>()
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        for (networkInterface in networkInterfaces) {
            if (!networkInterface.isUp || networkInterface.isLoopback) continue

            val addresses = networkInterface.inetAddresses
            for (address in addresses) {
                if (address.isSiteLocalAddress && !address.isAnyLocalAddress && address.hostAddress.startsWith(
                        subnet
                    )
                ) {
                    connectedDevices.add(address)
                }
            }
        }

        return connectedDevices
    }

    private fun getMacAddress(address: InetAddress): String {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        for (networkInterface in networkInterfaces) {
            val addresses = networkInterface.inetAddresses

            for (inetAddress in addresses) {
                if (inetAddress == address) {
                    val mac = networkInterface.hardwareAddress
                    val macBuilder = StringBuilder()

                    for (byte in mac) {
                        macBuilder.append(String.format("%02X:", byte))
                    }

                    if (macBuilder.isNotEmpty()) {
                        macBuilder.deleteCharAt(macBuilder.length - 1)
                    }

                    return macBuilder.toString()
                }
            }
        }

        return ""
    }
}