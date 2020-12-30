package com.zcu.kiv.pia.tictactoe.service

import io.ktor.websocket.*



abstract class RealtimeMessage() {
    enum class Type {
        FRIEND_REQUEST_NOTIFICATION,
        USERS_ONLINE,
        USER_ONLINE,
        USER_OFFLINE
    }
}

interface RealtimeService {
    fun addConnection(connection: DefaultWebSocketServerSession)

    fun addMessage()
}