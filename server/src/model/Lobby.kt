package com.zcu.kiv.pia.tictactoe.model

class Lobby(val id: Int, val owner: User, val boardSize: Int, val victoriousCells: Int) {
    var opponent: User? = null
    val invitedUsers = mutableListOf<User>()

    fun isFull() = opponent != null

    override fun equals(other: Any?) = other is Lobby && other.id == this.id
}