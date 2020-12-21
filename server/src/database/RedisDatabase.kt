package com.zcu.kiv.pia.tictactoe.database

import redis.clients.jedis.Jedis

private const val REDIS_ADDRESS = "redis"
private const val REDIS_PORT = 6379
private const val REDIS_DB_NAME = "ttt_game"

class RedisDatabase {
    val client = Jedis(REDIS_ADDRESS, REDIS_PORT).also {
        it.connect()
    }
    val dbName = REDIS_DB_NAME
}