package com.zcu.kiv.pia.tictactoe.service

import com.auth0.jwt.exceptions.JWTVerificationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.model.User
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*


class RealtimeMessage(namespace: Namespace, val action: String, val data: Any) {
    val namespace = namespace.name.toLowerCase()

    enum class Namespace {
        USERS,
        NOTIFICATIONS,
        FRIENDS,
        FRIENDREQUESTS,
        GAME
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
    interface ConnectionStatusListener {
        fun onConnected(user: User)
        fun onDisconnected(user: User)
    }

    fun addConnection(connection: DefaultWebSocketServerSession, user: User? = null)
    fun isConnected(user: User): Boolean
    fun removeConnection(connection: DefaultWebSocketServerSession, user: User? = null)
    fun addMessage(message: RealtimeMessage)
    fun addMessage(message: String, connection: DefaultWebSocketServerSession)
    fun sendMessage(message: RealtimeMessage, allUsers: Boolean = false, exclude: User? = null, vararg users: User)
    fun addOnConnectionStartedListener(listener: ConnectionStatusListener)
    fun removeOnConnectionStartedListener(listener: ConnectionStatusListener)
}

class WebsocketService(private val configurationService: ConfigurationService) : RealtimeService {
    private val connections = Collections.synchronizedSet(mutableSetOf<DefaultWebSocketServerSession>())
    private val usersToConnections = Collections.synchronizedMap(mutableMapOf<User, DefaultWebSocketServerSession>())
    private val connectionsToUsers = Collections.synchronizedMap(mutableMapOf<DefaultWebSocketServerSession, User>())
    private val onConnectionStoppedListeners =
        Collections.synchronizedSet(mutableSetOf<RealtimeService.ConnectionStatusListener>())
    private val onConnectionStartedListeners =
        Collections.synchronizedSet(mutableSetOf<RealtimeService.ConnectionStatusListener>())
//    private val messageListeners =
//        Collections.synchronizedMap(mutableMapOf<RealtimeMessage.Type, List<RealtimeMessageListener>>())

    private val jsonMapper = ObjectMapper().registerModule(KotlinModule())
    val messageParser = SimpleMessageParser()

    override fun isConnected(user: User) = usersToConnections.containsKey(user)

    override fun addConnection(connection: DefaultWebSocketServerSession, user: User?) {
        user?.let {
            logger.info { "User ${it.username} authenticated via WS" }
            if (usersToConnections.containsKey(it)) {
                logger.error { "Trying to add connection that already belongs to user ${it.username}" }
            }
            connectionsToUsers[connection] = user
            usersToConnections[it] = connection
            onConnectionStartedListeners.forEach { listener -> listener.onConnected(it) }
        }
        if (connections.contains(connection)) {
            logger.error { "Trying to add connection that is already exists in the set of connections" }
        }
        connections.add(connection)

    }

    override fun removeConnection(connection: DefaultWebSocketServerSession, user: User?) {
        user?.let {
            if (!usersToConnections.containsKey(it)) {
                logger.error { "Trying to remove a connection that has no user assigned to" }
            }
            usersToConnections.remove(it)
        }
        connectionsToUsers[connection]?.let { connUser ->
            logger.info { "Notifying that user ${connUser.username} disconnected" }
            onConnectionStartedListeners.forEach{ listener ->
                listener.onDisconnected(connUser)
            }
            usersToConnections.remove(connUser)
        }
        connectionsToUsers.remove(connection)
        connections.remove(connection)
    }

    override fun addMessage(message: String, connection: DefaultWebSocketServerSession) {
        messageParser.parse(message)?.let {
            if (it.first == "jwt") {
                val jwtConfig = JwtConfig(configurationService.jwtIssuer, configurationService.jwtSecret, 10 * 60)
                val principal = try {

                    val p = jwtConfig.verifier.verify(it.second)
                    val email = p.getClaim("email").isNull
                    val id = p.getClaim("id").isNull
                    val username = p.getClaim("username").isNull
                    if (email || id || username)
                        throw JWTVerificationException("Failed to retrieve all required fields from the JWT token")
                    else
                        JWTPrincipal(p)
                } catch (ex: JWTVerificationException) {
                    logger.warn { "Failed to verify JWT token" }
                    logger.error { ex }
                    return
                }
                addConnection(connection, User.fromJWTToken(principal))
            }
        } ?: run {
            logger.warn { "Invalid message received $message" }
        }
    }

    override fun addMessage(message: RealtimeMessage) {
        // messageListeners[message.type]?.forEach { it.receiveMessage(message) }
    }

    override fun sendMessage(message: RealtimeMessage, allUsers: Boolean, exclude: User?, vararg users: User) {
        val json = jsonMapper.writeValueAsString(message)
        val frame = Frame.Text(json)
        logger.info { "Sending frame of \"$json\"" }
        GlobalScope.launch {
            if (allUsers) {
                logger.info { "Sending to all users..." }
                usersToConnections.filter { it.key != exclude }.forEach {
                    logger.info { "Sending a message to ${it.key.username}..." }
                    it.value.send(Frame.Text(json))
                }
            } else {
                logger.info { "Sending to specific users..." }
                users.forEach {
                    usersToConnections[it]?.send(frame)
                }
            }
        }
    }

    //override fun addListener(listener: RealtimeMessageListener, type: RealtimeMessage.Type) {
    //    TODO("Not yet implemented")
    //}

    override fun addOnConnectionStartedListener(listener: RealtimeService.ConnectionStatusListener) {
        onConnectionStartedListeners.add(listener)
    }

    override fun removeOnConnectionStartedListener(listener: RealtimeService.ConnectionStatusListener) {
        onConnectionStartedListeners.remove(listener)
    }
}