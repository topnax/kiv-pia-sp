package com.zcu.kiv.pia.tictactoe.controller

import com.zcu.kiv.pia.tictactoe.model.response.SuccessResponse
import com.zcu.kiv.pia.tictactoe.model.response.realtime.LobbyInvitesResponse
import com.zcu.kiv.pia.tictactoe.request.LeaveLobbyRequest
import com.zcu.kiv.pia.tictactoe.request.StartLobbyRequest
import com.zcu.kiv.pia.tictactoe.request.game.AcceptInviteRequest
import com.zcu.kiv.pia.tictactoe.request.game.CreateGameRequest
import com.zcu.kiv.pia.tictactoe.request.game.DeclineInviteRequest
import com.zcu.kiv.pia.tictactoe.request.game.InviteToGameRequest
import com.zcu.kiv.pia.tictactoe.service.LobbyService
import com.zcu.kiv.pia.tictactoe.service.UserService
import com.zcu.kiv.pia.tictactoe.utils.*
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

val lobbyLogger = KotlinLogging.logger {}

fun Route.lobbyRoutes() {

    // TODO user mustn't be present in a game

    jwtAuthenticatedRoute("/lobby") {
        val lobbyService: LobbyService by inject()
        val userService: UserService by inject()

        post("/create") {
            val request = call.receive<CreateGameRequest>()
            tryRun {
                lobbyService.createLobby(getLoggedUser(), request.boardSize, request.victoriousCells)
                successResponse()
            }
        }

        post("/invite") {
            val request = call.receive<InviteToGameRequest>()
            val user = getLoggedUser()

            tryRun {
                userService.getUserById(request.userId)?.let { userToBeInvited ->
                    lobbyService.getLobby(user)?.let { lobby ->
                        lobbyService.inviteUser(userToBeInvited, lobby, user)
                        successResponse()
                    } ?: run {
                        errorResponse("User calling not present in any lobby")
                    }
                } ?:run {
                    errorResponse("User to be invited not found")
                }
            }
        }

        post("/accept") {
            val request = call.receive<AcceptInviteRequest>()
            val user = getLoggedUser()

            tryRun {
                lobbyService.getLobby(request.lobbyId)?.let {
                    lobbyService.acceptInvite(it, user)
                    successResponse()
                } ?: run {
                    errorResponse("Lobby not found")
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
                    lobbyLogger.error { "sent response" }
                } ?: run {
                    errorResponse("Lobby not found")
                    lobbyLogger.error { "sent response 2" }
                }
            }
        }

        post("/start") {
            val request = call.receive<StartLobbyRequest>()
            val user = getLoggedUser()

            tryRun {
                lobbyService.getLobby(request.lobbyId)?.let {
                    lobbyService.startLobby(it, user)
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
        if (it !is LobbyService.LobbyServiceException) {
            lobbyLogger.error{"sent response 3"}
            lobbyLogger.error { it }
            errorResponse("Unknown error")
        } else {

            lobbyLogger.error{"sent response 4"}
            errorResponse(it.reason)
        }
    }