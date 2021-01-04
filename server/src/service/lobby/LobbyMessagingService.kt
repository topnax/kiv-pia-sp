package com.zcu.kiv.pia.tictactoe.service.lobby

import com.zcu.kiv.pia.tictactoe.model.Lobby
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.realtime.InviteGoneResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.LobbyDestroyedResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.LobbyStateResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.NewInviteResponse
import com.zcu.kiv.pia.tictactoe.service.RealtimeMessage
import com.zcu.kiv.pia.tictactoe.service.RealtimeService

interface LobbyMessagingService {
    fun sendNewInviteNotification(lobby: Lobby, invitedUser: User)

    fun sendGoneInviteNotification(lobby: Lobby, invitedUser: User)

    fun sendLobbyState(lobby: Lobby, user: User)

    fun sendLobbyDestroyedNotification(lobby: Lobby, user: User)
}

class LobbyMessagingServiceImpl(private val realtimeService: RealtimeService) : LobbyMessagingService {
    override fun sendNewInviteNotification(lobby: Lobby, invitedUser: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.LOBBY,
                "newInvite",
                NewInviteResponse(
                    lobby.id,
                    lobby.owner.username
                )
            ),
            users = arrayOf(invitedUser)
        )
    }

    override fun sendGoneInviteNotification(lobby: Lobby, invitedUser: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.LOBBY,
                "goneInvite",
                InviteGoneResponse(
                    lobby.id
                )
            ),
            users = arrayOf(invitedUser)
        )
    }

    override fun sendLobbyState(lobby: Lobby, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.LOBBY,
                "state",
                LobbyStateResponse(
                    lobby,
                    lobby.owner == user
                )
            ),
            users = arrayOf(user)
        )
    }

    override fun sendLobbyDestroyedNotification(lobby: Lobby, user: User) {
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.LOBBY,
                "destroyed",
                LobbyDestroyedResponse(
                    lobby.id
                )
            ),
            users = arrayOf(user)
        )
    }
}