package com.yh.paymentwebflux.payment.adapter.`in`.web.response

import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val status: Int = 200,
    val message: String = "",
    val data: T? = null
){


    companion object {

        fun <T> with(status: HttpStatus, message: String, data: T?): ApiResponse<T> {
            return ApiResponse(
                status = status.value(),
                message = message,
                data = data
            )
        }
    }
}