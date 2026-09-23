package com.example.network

import com.example.data.AppLogger
import com.example.network.NetworkConstants.UDP_PORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

object DiscoveryService {
    private const val TAG = "DiscoveryService"

    /**
     * Responde a peticiones de descubrimiento (Profesor)
     */
    suspend fun runDiscoveryResponder(
        shouldContinue: () -> Boolean,
        provideOffer: () -> JSONObject,
        onSocketCreated: (DatagramSocket) -> Unit
    ) = withContext(Dispatchers.IO) {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket(null).apply {
                reuseAddress = true
                bind(java.net.InetSocketAddress(UDP_PORT))
            }
            onSocketCreated(socket!!)
            val buffer = ByteArray(1024)
            AppLogger.d(TAG, "UDP Discovery Responder started on port $UDP_PORT")

            while (shouldContinue()) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    socket.receive(packet)
                    val received = String(packet.data, 0, packet.length).trim()
                    
                    if (received == "AULA_DISCOVER") {
                        val replyJson = provideOffer()
                        val replyBytes = replyJson.toString().toByteArray()
                        val replyPacket = DatagramPacket(
                            replyBytes,
                            replyBytes.size,
                            packet.address,
                            packet.port
                        )
                        socket.send(replyPacket)
                        AppLogger.d(TAG, "UDP Offer sent to ${packet.address.hostAddress}")
                    }
                } catch (e: Exception) {
                    if (shouldContinue()) AppLogger.e(TAG, "Error in UDP receive", e)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "UDP Responder failed", e)
        } finally {
            socket?.close()
        }
    }

    /**
     * Busca un profesor activo (Alumno)
     */
    suspend fun discoverTeacher(
        onSuccess: (ip: String, port: Int, teacherName: String, sectionName: String) -> Unit,
        onFailure: () -> Unit
    ) = withContext(Dispatchers.IO) {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2500

            val discoverMsg = "AULA_DISCOVER".toByteArray()
            val addresses = mutableListOf<InetAddress>()
            addresses.add(InetAddress.getByName("255.255.255.255"))
            
            // Intentar con todas las interfaces para mayor cobertura
            try {
                val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    if (!intf.isUp || intf.isLoopback) continue
                    for (ifaceAddr in intf.interfaceAddresses) {
                        ifaceAddr.broadcast?.let { addresses.add(it) }
                    }
                }
            } catch (e: Exception) {}

            for (address in addresses.distinct()) {
                val packet = DatagramPacket(discoverMsg, discoverMsg.size, address, UDP_PORT)
                try { socket.send(packet) } catch (e: Exception) {}
            }

            val buffer = ByteArray(2048)
            val receivePacket = DatagramPacket(buffer, buffer.size)

            try {
                socket.soTimeout = 3500 
                socket.receive(receivePacket)
                val responseText = String(receivePacket.data, 0, receivePacket.length).trim()
                val json = JSONObject(responseText)
                
                if (json.optString("type") == "AULA_OFFER") {
                    val ip = receivePacket.address.hostAddress ?: "192.168.43.1"
                    val teacherName = json.getString("teacherName")
                    val sectionName = json.getString("sectionName")
                    val tcpPort = json.optInt("tcpPort", NetworkConstants.DEFAULT_TCP_PORT)

                    withContext(Dispatchers.Main) {
                        onSuccess(ip, tcpPort, teacherName, sectionName)
                    }
                    return@withContext
                }
            } catch (timeout: SocketTimeoutException) {
                AppLogger.d(TAG, "Discovery timed out, using fallback scan...")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Discovery failed", e)
        } finally {
            socket?.close()
        }

        // Fallback: Escaneo de puertas de enlace comunes
        val gateways = mutableListOf("192.168.43.1", "192.168.49.1", "192.168.1.1", "192.168.0.1")
        try {
            val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in java.util.Collections.list(intf.inetAddresses)) {
                    if (addr is java.net.Inet4Address) {
                        addr.hostAddress?.let { ip ->
                            if (ip.contains(".")) {
                                val subnet = ip.substringBeforeLast(".")
                                gateways.add("$subnet.1")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {}

        for (ip in gateways.distinct()) {
            if (pingHost(ip, NetworkConstants.DEFAULT_TCP_PORT)) {
                withContext(Dispatchers.Main) {
                    onSuccess(ip, NetworkConstants.DEFAULT_TCP_PORT, "Profesor", "Clase Detectada")
                }
                return@withContext
            }
        }

        withContext(Dispatchers.Main) { onFailure() }
    }

    private fun pingHost(ip: String, port: Int): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(ip, port), 800)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
