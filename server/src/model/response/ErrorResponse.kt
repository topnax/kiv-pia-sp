package com.zcu.kiv.pia.tictactoe.model.response

class ErrorResponse(message: String) : BaseResponse(ResponseCode.ERROR.code, message)