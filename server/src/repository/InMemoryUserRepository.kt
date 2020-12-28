package com.zcu.kiv.pia.tictactoe.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.zcu.kiv.pia.tictactoe.database.RedisDatabase
import com.zcu.kiv.pia.tictactoe.model.User

interface InMemoryUserRepository {
    fun addLoggedInUser(user: User)
    fun removeLoggedInUser(user: User)
    fun getLoggedInUsers(): List<User>
}

class RedisUserRepository(private val redis: RedisDatabase): InMemoryUserRepository {
    private val jsonMapper = ObjectMapper().registerModule(KotlinModule())
    private val dbName = redis.dbName

    override fun addLoggedInUser(user: User) {
        redis.client.sadd("$dbName:loggedin", jsonMapper.writeValueAsString(user))
    }

    override fun removeLoggedInUser(user: User) {
        redis.client.srem("$dbName:loggedin", jsonMapper.writeValueAsString(user))
    }

    override fun getLoggedInUsers(): List<User> {
        val users = redis.client.smembers("${dbName}:loggedin")
        return users.map {
            jsonMapper.readValue<User>(it)
        }.toList()
    }
}