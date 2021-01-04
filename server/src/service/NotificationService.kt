package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.Lobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.NotificationResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.LobbyStateResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.NewInviteResponse
import com.zcu.kiv.pia.tictactoe.service.RealtimeMessage
import com.zcu.kiv.pia.tictactoe.service.RealtimeService

interface NotificationService {
    fun sendNotification(text: String, user: User)
}

class NotificationServiceImpl(private val realtimeService: RealtimeService) : NotificationService {
    override fun sendNotification(text: String, user: User) = realtimeService.sendMessage(
        RealtimeMessage(
            RealtimeMessage.Namespace.NOTIFICATIONS,
            "new",
            NotificationResponse(
                text
            ),
        ),
        users = arrayOf(user)
    )
}