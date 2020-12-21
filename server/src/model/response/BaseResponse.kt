package com.zcu.kiv.pia.tictactoe.model.response

open class BaseResponse(
    val responseCode: Int, val message: String
)

class SuccessResponse(): BaseResponse(ResponseCode.SUCCESS.code, "")

enum class ResponseCode(val code: Int) {
    SUCCESS(0),
    ERROR(100)
}