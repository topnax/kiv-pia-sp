package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.model.response.realtime.LobbyInvitesResponse
import com.zcu.kiv.pia.tictactoe.request.LeaveLobbyRequest
import com.zcu.kiv.pia.tictactoe.request.StartLobbyRequest
import com.zcu.kiv.pia.tictactoe.request.game.AcceptInviteRequest
import com.zcu.kiv.pia.tictactoe.request.game.CreateGameRequest
import com.zcu.kiv.pia.tictactoe.request.game.DeclineInviteRequest
import com.zcu.kiv.pia.tictactoe.request.game.InviteToGameRequest
import com.zcu.kiv.pia.tictactoe.service.LobbyService
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.service.game.GameService
import com.zcu.kiv.pia.tictactoe.utils.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

val lobbyLogger = KotlinLogging.logger {}

fun Route.lobbyRoutes() {

    jwtAuthenticatedRoute("/lobby") {
        val lobbyService: LobbyService by inject()
        val gameService: GameService by inject()
        val userService: UserService by inject()

        post("/create") {
            val request = call.receive<CreateGameRequest>()
            val user = getLoggedUser()
            if (!gameService.isUserPlaying(user)) {
                tryRun {
                    lobbyService.createLobby(getLoggedUser(), request.boardSize, request.victoriousCells)
                    successResponse()
                }
            } else {
                errorResponse("User is already participating in a game")
            }
        }

        post("/invite") {
            val request = call.receive<InviteToGameRequest>()
            val user = getLoggedUser()

            tryRun {
                if (!gameService.isUserPlaying(user)) {
                    userService.getUserById(request.userId)?.let { userToBeInvited ->
                        if (!gameService.isUserPlaying(userToBeInvited)) {
                            lobbyService.getLobby(user)?.let { lobby ->
                                lobbyService.inviteUser(userToBeInvited, lobby, user)
                                successResponse()
                            } ?: run {
                                errorResponse("User calling not present in any lobby")
                            }
                        } else {
                            errorResponse("Invited user is already playing a game")
                        }
                    } ?: run {
                        errorResponse("User to be invited not found")
                    }
                } else {
                    errorResponse("Cannot send an invite when participating in a game")
                }
            }
        }

        post("/accept") {
            val request = call.receive<AcceptInviteRequest>()
            val user = getLoggedUser()

            tryRun {
                if (!gameService.isUserPlaying(user)) {
                    lobbyService.getLobby(request.lobbyId)?.let {
                        lobbyService.acceptInvite(it, user)
                        successResponse()
                    } ?: run {
                        errorResponse("Lobby not found")
                    }
                } else {
                    errorResponse("User already playing a game")
                }
            }
        }

        post("/decline") {
            val request = call.receive<DeclineInviteRequest>()
            val user = getLoggedUser()

            tryRun {
                lobbyService.getLobby(request.lobbyId)?.let {
                    lobbyService.declineInvite(it, user)
                    successResponse()
                } ?: run {
                    errorResponse("Lobby not found")
                }
            }
        }

        post("/leave") {
            val request = call.receive<LeaveLobbyRequest>()
            val user = getLoggedUser()

            tryRun {
                lobbyService.getLobby(request.lobbyId)?.let {
                    lobbyService.leaveLobby(it, user)
                    successResponse()
                } ?: run {
                    errorResponse("Lobby not found")
                }
            }
        }

        post("/start") {
            val request = call.receive<StartLobbyRequest>()
            val user = getLoggedUser()

            tryRun {
                lobbyService.getLobby(request.lobbyId)?.let {
                    lobbyService.startLobby(it, user)
                    gameService.createGame(it.owner, it.opponent!!, it.boardSize, it.victoriousCells)
                    successResponse()
                } ?: run {
                    errorResponse("Lobby not found")
                }
            }
        }

        get("/invites") {
            dataResponse(LobbyInvitesResponse(lobbyService.getInvites(getLoggedUser())))
        }
    }

}

suspend inline fun PipelineContext<Unit, ApplicationCall>.tryRun(unit: () -> Unit) =
    runCatching { unit() }.exceptionOrNull()?.also {
        // TODO generalize this
        when (it) {
            is LobbyService.LobbyServiceException -> {
                errorResponse(it.reason)
            }
            is GameService.GameServiceException -> {
                errorResponse(it.reason)
            }
            else -> {
                lobbyLogger.error { it }
                errorResponse("Unknown error")
            }
        }
    }