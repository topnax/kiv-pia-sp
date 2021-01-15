package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.NotificationResponse

interface NotificationService {
    /**
     * Sends a text notification to the given user
     */
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