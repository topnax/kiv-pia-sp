package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.model.response.UserOfflineResponse
import com.zcu.kiv.pia.tictactoe.model.response.UserOnlineResponse
import com.zcu.kiv.pia.tictactoe.model.response.UsersOnlineResponse
import com.zcu.kiv.pia.tictactoe.repository.InMemoryUserRepository
import com.zcu.kiv.pia.tictactoe.repository.PersistentUserRepository
import mu.KotlinLogging

interface UserService {
    /**
     * Adds a user to a persistent database
     */
    suspend fun addUser(email: String, username: String, password: String)

    /**
     * Searches for a user in a persistent database by the specified id
     */
    suspend fun getUserById(id: Int): User?

    /**
     * Searches for a user in a persistent database by the specified email
     */
    suspend fun getUserByEmail(email: String): User?

    /**
     * Searches for a user in a persistent database by the specified username
     */
    suspend fun getUserByUsername(username: String): User?

    /**
     * Returns all users in the persistent database
     */
    suspend fun getUsers(): List<User>

    /**
     * Tries to find a user by credentials in a persistent database
     */
    suspend fun getUserByCredentials(email: String, passwordHash: String): User?

    /**
     * Changes password of the given  user
     */
    suspend fun changeUserPassword(user: User, passwordHash: String)

    /**
     * Adds a logged in user to an in-memory database
     */
    fun addLoggedInUser(user: User)

    /**
     * Removes a logged in user from an in-memory database
     */
    fun removeLoggedInUser(user: User)

    /**
     * Returns all logged in users from an in-memory database
     */
    fun getLoggedInUsers(): List<User>


    /**
     * Returns `true` when the given user is online
     */
    fun isUserOnline(user: User): Boolean

    /**
     * Promotes the given user to admin
     */
    suspend fun promoteUserToAdmin(user: User)

    /**
     * Demotes the given admin to user
     */
    suspend fun demoteAdminToUser(user: User)

}

private val logger = KotlinLogging.logger {}

class UserServiceImpl(
    private val persistentUserRepository: PersistentUserRepository,
    private val inMemoryUserRepository: InMemoryUserRepository,
    private val realtimeService: RealtimeService,
    private val notificationService: NotificationService
) : UserService {

    init {
        realtimeService.addConnectionStatusListener(
            object : RealtimeService.ConnectionStatusListener {

                override fun onConnected(user: User) {
                    addLoggedInUser(user)
                    realtimeService.sendMessage(
                        RealtimeMessage(
                            RealtimeMessage.Namespace.USERS,
                            "onlineUsers",
                            UsersOnlineResponse(inMemoryUserRepository.getLoggedInUsers()),
                        ),
                        users = arrayOf(user)
                    )
                }

                override fun onDisconnected(user: User) {
                    logger.info { "UserServiceImpl ${user.username} disconnected" }
                    removeLoggedInUser(user)
                }

            }
        )
    }

    override fun isUserOnline(user: User) = inMemoryUserRepository.getLoggedInUsers().contains(user)

    override suspend fun addUser(email: String, username: String, password: String) =
        persistentUserRepository.addUser(email, username, password)

    override suspend fun getUserByEmail(email: String) = persistentUserRepository.getUserByEmail(email)

    override suspend fun getUserById(id: Int) = persistentUserRepository.getUserById(id)

    override suspend fun getUserByUsername(username: String) = persistentUserRepository.getUserByUsername(username)

    override suspend fun getUsers() = persistentUserRepository.getUsers()

    override suspend fun getUserByCredentials(email: String, passwordHash: String) =
        persistentUserRepository.userByCredentials(email, passwordHash)

    override suspend fun changeUserPassword(user: User, passwordHash: String) {
        persistentUserRepository.updateUserPassword(user, passwordHash)
    }

    override fun addLoggedInUser(user: User) {
        // TODO do not allow two connections
        inMemoryUserRepository.addLoggedInUser(user)
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.USERS,
                "userOnline",
                UserOnlineResponse(user)
            ), allUsers = true, exclude = user
        )
    }

    override fun removeLoggedInUser(user: User) {
        // TODO should remove connection :thinking-emoji:
        realtimeService.sendMessage(
            RealtimeMessage(
                RealtimeMessage.Namespace.USERS,
                "offlineUser",
                UserOfflineResponse(user)
            ), allUsers = true, exclude = user
        )
        inMemoryUserRepository.removeLoggedInUser(user)
    }

    override fun getLoggedInUsers() = inMemoryUserRepository.getLoggedInUsers()

    override suspend fun promoteUserToAdmin(user: User) =
        persistentUserRepository.updateUserAdminStatus(user, true).also {
            notificationService.sendNotification("Your role has been changed, please refresh the page.", user)
        }

    override suspend fun demoteAdminToUser(user: User) =
        persistentUserRepository.updateUserAdminStatus(user, false).also {
            notificationService.sendNotification("Your role has been changed, please refresh the page.", user)
        }
}
