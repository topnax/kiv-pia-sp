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

private val logger = KotlinLogging.logger { }

/**
 * A class representing a message sent over websocket connection
 */
class RealtimeMessage(namespace: Namespace, val action: String, val data: Any) {
    /**
     * Namespace the message belongs to
     */
    val namespace = namespace.name.toLowerCase()

    enum class Namespace {
        USERS,
        NOTIFICATIONS,
        FRIENDS,
        FRIENDREQUESTS,
        GAME,
        LOBBY
    }
}

/**
 * Realtime message listener
 */
interface RealtimeMessageListener {
    fun receiveMessage(message: RealtimeMessage)
}

/**
 * A simple message parser used for parsing incoming messages (used only when authenticating)
 */
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
    /**
     * Definition of realtime connection status listener
     */
    interface ConnectionStatusListener {
        fun onConnected(user: User)
        fun onDisconnected(user: User)
    }

    /**
     * Adds a new realtime connection
     */
    fun addConnection(connection: DefaultWebSocketServerSession, user: User? = null)

    /**
     * Checks whether a user is connected to the realtime service
     */
    fun isConnected(user: User): Boolean

    /**
     * Removes a realtime connection. Specify a user to detach the user from the given connection
     */
    fun removeConnection(connection: DefaultWebSocketServerSession, user: User? = null)

    /**
     * Receive a realtime message
     */
    fun receiveMessage(message: RealtimeMessage)

    /**
     * Receive a raw string message from the given connection
     */
    fun receiveMessage(message: String, connection: DefaultWebSocketServerSession)

    /**
     * Send a message to given users
     */
    fun sendMessage(message: RealtimeMessage, allUsers: Boolean = false, exclude: User? = null, vararg users: User)

    /**
     * Add a connection status listener
     */
    fun addConnectionStatusListener(listener: ConnectionStatusListener)

    /**
     * Remove a connection status listener
     */
    fun removeConnectionStatusListener(listener: ConnectionStatusListener)
}

class WebsocketService(private val configurationService: ConfigurationService) : RealtimeService {
    /**
     * A set of websocket connections
     */
    private val connections = Collections.synchronizedSet(mutableSetOf<DefaultWebSocketServerSession>())

    /**
     * Users mapped to connections
     */
    private val usersToConnections = Collections.synchronizedMap(mutableMapOf<Int, DefaultWebSocketServerSession>())

    /**
     * Connections mapped to users
     */
    private val connectionsToUsers = Collections.synchronizedMap(mutableMapOf<DefaultWebSocketServerSession, User>())

    /**
     * A set of connection status listeners
     */
    private val connectionStatusListeners =
        Collections.synchronizedSet(mutableSetOf<RealtimeService.ConnectionStatusListener>())

    /**
     * JSON mapper
     */
    private val jsonMapper = ObjectMapper().registerModule(KotlinModule())

    /**
     * Simple incoming message parser
     */
    private val messageParser = SimpleMessageParser()

    override fun isConnected(user: User) = usersToConnections.containsKey(user.id)

    override fun addConnection(connection: DefaultWebSocketServerSession, user: User?) {
        user?.let {
            logger.info { "User #${it.id}-${it.username} authenticated via WS" }
            if (usersToConnections.containsKey(it.id)) {
                logger.error { "Trying to add connection that already belongs to user ${it.username}" }
            }
            connectionsToUsers[connection] = user
            usersToConnections[it.id] = connection
            connectionStatusListeners.forEach { listener -> listener.onConnected(it) }
        }
        if (connections.contains(connection)) {
            logger.error { "Trying to add connection that is already exists in the set of connections" }
        }
        connections.add(connection)

    }

    override fun removeConnection(connection: DefaultWebSocketServerSession, user: User?) {
        user?.let {
            if (!usersToConnections.containsKey(it.id)) {
                logger.error { "Trying to remove a connection that has no user assigned to" }
            }
            usersToConnections.remove(it.id)
        }
        connectionsToUsers[connection]?.let { connUser ->
            logger.info { "Notifying that user #${connUser.id}-${connUser.username} disconnected" }
            connectionStatusListeners.forEach { listener ->
                listener.onDisconnected(connUser)
            }
            usersToConnections.remove(connUser.id)
        }
        connectionsToUsers.remove(connection)
        connections.remove(connection)
    }

    override fun receiveMessage(message: String, connection: DefaultWebSocketServerSession) {
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

    override fun receiveMessage(message: RealtimeMessage) {

    }

    override fun sendMessage(message: RealtimeMessage, allUsers: Boolean, exclude: User?, vararg users: User) {
        val json = jsonMapper.writeValueAsString(message)
        val frame = Frame.Text(json)
        logger.info { "Sending frame of \"$json\"" }
        GlobalScope.launch {
            if (allUsers) {
                logger.info { "Sending to all users..." }
                usersToConnections.filter { it.key != exclude?.id }.forEach {
                    logger.info { "Sending to id ${it.key}..." }
                    it.value.send(Frame.Text(json))
                }
            } else {
                logger.info { "Sending to specific users [${users.joinToString { it.id.toString() + "#" + it.username }}]..." }
                users.forEach {
                    usersToConnections[it.id]?.send(frame)
                }
            }
        }
    }

    override fun addConnectionStatusListener(listener: RealtimeService.ConnectionStatusListener) {
        connectionStatusListeners.add(listener)
    }

    override fun removeConnectionStatusListener(listener: RealtimeService.ConnectionStatusListener) {
        connectionStatusListeners.remove(listener)
    }
}