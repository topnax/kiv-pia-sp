package com.zcu.kiv.pia.tictactoe.model

import java.time.LocalDateTime

class GameResult(val id: Int, val crossWon: Boolean, val crossUserId: Int, val noughtUserId: Int, val dateCreated: LocalDateTime, val boardSize: Int) {
}