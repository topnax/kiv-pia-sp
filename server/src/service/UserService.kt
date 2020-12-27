package com.zcu.kiv.pia.tictactoe.service

import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.repository.InMemoryUserRepository
import com.zcu.kiv.pia.tictactoe.repository.PersistentUserRepository

interface UserService {
    /**
     * Adds a user to a persistent database
     */
    suspend fun addUser(email: String, username: String, password: String)

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

}


class UserServiceImpl(
    private val persistentUserRepository: PersistentUserRepository,
    private val inMemoryUserRepository: InMemoryUserRepository
) : UserService {

    override suspend fun addUser(email: String, username: String, password: String) =
        persistentUserRepository.addUser(email, username, password)

    override suspend fun getUserByEmail(email: String) = persistentUserRepository.getUserByEmail(email)

    override suspend fun getUserByUsername(username: String) = persistentUserRepository.getUserByUsername(username)

    override suspend fun getUsers() = persistentUserRepository.getUsers()

    override suspend fun getUserByCredentials(email: String, passwordHash: String) =
        persistentUserRepository.userByCredentials(email, passwordHash)

    override suspend fun changeUserPassword(user: User, passwordHash: String) {
        persistentUserRepository.updateUserPassword(user, passwordHash)
    }

    override fun addLoggedInUser(user: User) {
        inMemoryUserRepository.addLoggedInUser(user)
    }

    override fun removeLoggedInUser(user: User) {
        inMemoryUserRepository.removeLoggedInUser(user)
    }

    override fun getLoggedInUsers() = inMemoryUserRepository.getLoggedInUsers()

}
