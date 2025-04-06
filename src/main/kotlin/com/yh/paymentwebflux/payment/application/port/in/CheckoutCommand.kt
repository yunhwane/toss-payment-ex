package com.yh.paymentwebflux.payment.application.port.`in`

data class CheckoutCommand(
    val certId: Long,
    val productIds: List<Long>,
    val buyerId: Long,
    val idempotencyKey: String
)
