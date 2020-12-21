package com.zcu.kiv.pia.tictactoe.request

class ChangePasswordRequest(val email: String, val password: String, val passwordConfirm: String, val currentPassword: String)