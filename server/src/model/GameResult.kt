package com.zcu.kiv.pia.tictactoe.model

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GameResult(val id: Int, val draw: Boolean, val crossWon: Boolean, val crossUserId: Int, val noughtUserId: Int, dateCreated: LocalDateTime, val boardSize: Int) {
    val dateCreated = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(dateCreated)
    var crossUsername: String? = null
    var noughtUsername: String? = null
}