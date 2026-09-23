package com.example.network

import com.example.network.NetworkConstants.ACTION_CHECK_IN
import com.example.network.NetworkConstants.ACTION_CHECK_OUT
import com.example.network.NetworkConstants.ACTION_GET_CLASS_INFO
import com.example.network.NetworkConstants.ACTION_GET_FILE_CHUNK
import com.example.network.NetworkConstants.ACTION_SYNC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object ClassroomClient {

    private suspend fun sendRequest(ip: String, port: Int, request: JSONObject): JSONObject? = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.connect(java.net.InetSocketAddress(ip, port), 10000)
            socket.soTimeout = 15000
            
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            
            writer.println(request.toString())
            val line = reader.readLine()
            if (line != null) return@withContext JSONObject(line)
            else com.example.data.AppLogger.e("ClassroomClient", "Servidor cerró conexión prematuramente")
        } catch (e: Exception) {
            com.example.data.AppLogger.e("ClassroomClient", "Error en request ($ip:$port): ${e.message}")
        } finally {
            socket?.close()
        }
        null
    }

    suspend fun fetchClassInfo(ip: String, port: Int) = sendRequest(ip, port, JSONObject().apply {
        put("action", ACTION_GET_CLASS_INFO)
    })

    suspend fun checkIn(ip: String, port: Int, data: JSONObject) = sendRequest(ip, port, data.apply {
        put("action", ACTION_CHECK_IN)
    })

    suspend fun sync(ip: String, port: Int, data: JSONObject) = sendRequest(ip, port, data.apply {
        put("action", ACTION_SYNC)
    })

    suspend fun checkOut(ip: String, port: Int, cardId: String) = sendRequest(ip, port, JSONObject().apply {
        put("action", ACTION_CHECK_OUT)
        put("cardId", cardId)
    })

    suspend fun fetchChunk(ip: String, port: Int, fileId: String, offset: Long, length: Int, cardId: String) = sendRequest(ip, port, JSONObject().apply {
        put("action", ACTION_GET_FILE_CHUNK)
        put("fileId", fileId)
        put("offset", offset)
        put("length", length)
        put("cardId", cardId)
    })
}
