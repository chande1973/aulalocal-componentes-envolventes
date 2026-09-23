package com.example.network

object NetworkConstants {
    const val UDP_PORT = 8888
    const val DEFAULT_TCP_PORT = 9999
    
    // Acciones del Protocolo
    const val ACTION_GET_CLASS_INFO = "get_class_info"
    const val ACTION_CHECK_IN = "check_in"
    const val ACTION_CHECK_OUT = "check_out"
    const val ACTION_SYNC = "sync"
    const val ACTION_SEND_DOUBT = "send_doubt"
    const val ACTION_CLEAR_DOUBT = "clear_doubt"
    const val ACTION_GET_FILE_CHUNK = "get_file_chunk"
    
    // Comandos de Respuesta
    const val CMD_ATTENDANCE_CHECK = "attendance_check"
    const val CMD_FORCE_DISCONNECT = "FORCE_DISCONNECT"
    
    // Status
    const val STATUS_OK = "ok"
    const val STATUS_ERROR = "error"
    const val STATUS_EXPELLED = "expelled"
    const val STATUS_EOF = "eof"
    const val STATUS_NOT_FOUND = "not_found"
    
    fun areSectionsEqual(s1: String, s2: String): Boolean {
        return s1.trim().lowercase() == s2.trim().lowercase()
    }
}
