package com.zcu.kiv.pia.tictactoe.service

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import com.zcu.kiv.pia.tictactoe.model.User
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*


abstract class RealtimeMessage(val type: Type) {
    enum class Type {
        FRIEND_REQUEST_NOTIFICATION,
        USERS_ONLINE,
        USER_ONLINE,
        USER_OFFLINE
    }
}

private val logger = KotlinLogging.logger { }

interface RealtimeMessageListener {
    fun receiveMessage(message: RealtimeMessage)
}

class SimpleMessageParser {
    private val delimiter = ";"
    fun parse(text: String): Pair<String, String>? {
        val delimiterPos = text.indexOf(delimiter)
        if (delimiterPos > 0 && delimiterPos < text.length - 1) {
            return Pair(text.substring(0 until delimiterPos), text.substring(delimiterPos + 1 until text.length))
        }
        return null
    }
}

interface RealtimeService {
    interface OnConnectionStarted {
        fun onConnected(user: User)
    }

    fun addConnection(connection: DefaultWebSocketServerSession, user: User)
    fun isConnected(user: User): Boolean
    fun removeConnection(connection: DefaultWebSocketServerSession, user: User)
    fun addMessage(message: RealtimeMessage)
    fun addMessage(message: String)
    fun sendMessage(message: RealtimeMessage, vararg users: User)
    fun addListener(listener: RealtimeMessageListener, type: RealtimeMessage.Type)
    fun addOnConnectionStartedListener(listener: OnConnectionStarted)
    fun removeOnConnectionStartedListener(listener: OnConnectionStarted)
}

class WebsocketService : RealtimeService {
    private val connections = Collections.synchronizedMap(mutableMapOf<User, DefaultWebSocketServerSession>())
    private val onConnectionStartedListener =
        Collections.synchronizedSet(mutableSetOf<RealtimeService.OnConnectionStarted>())
    private val messageListeners =
        Collections.synchronizedMap(mutableMapOf<RealtimeMessage.Type, List<RealtimeMessageListener>>())

    val messageParser = SimpleMessageParser()

    override fun isConnected(user: User) = connections.containsKey(user)

    override fun addConnection(connection: DefaultWebSocketServerSession, user: User) {
        if (connections.containsKey(user)) {
            logger.error { "Trying to add connection that already belongs to user ${user.username}" }
        }
        connections[user] = connection
        onConnectionStartedListener.forEach { it.onConnected(user) }
    }

    override fun removeConnection(connection: DefaultWebSocketServerSession, user: User) {
        if (!connections.containsKey(user)) {
            logger.error { "Trying to remove a connection that has no user assigned to" }
        }
        connections.remove(user)
    }

    override fun addMessage(message: String) {
        messageParser.parse(message)?.let {
           if (it.first == "jwt") {
               val token: DecodedJWT? = try {
                   JWT.decode(it.second)
               } catch (e: JWTDecodeException) {
                   logger.error { e }
                   null
               }
               token?.let {

                   User.fromJWTToken(JWTPrincipal(JWTParser().parsePayload(token.payload)))
               }
           }
        } ?: run {
           logger.warn { "Invalid message received $message" }
        }
    }

    override fun addMessage(message: RealtimeMessage) {
        messageListeners[message.type]?.forEach { it.receiveMessage(message) }
    }

    override fun sendMessage(message: RealtimeMessage, vararg users: User) {
        GlobalScope.launch {
            users.forEach {
                connections[it]?.send(Frame.Text("I am sending ${message.type}"))
            }
        }
    }

    override fun addListener(listener: RealtimeMessageListener, type: RealtimeMessage.Type) {
        TODO("Not yet implemented")
    }

    override fun addOnConnectionStartedListener(listener: RealtimeService.OnConnectionStarted) {
        TODO("Not yet implemented")
    }

    override fun removeOnConnectionStartedListener(listener: RealtimeService.OnConnectionStarted) {
        TODO("Not yet implemented")
    }
}