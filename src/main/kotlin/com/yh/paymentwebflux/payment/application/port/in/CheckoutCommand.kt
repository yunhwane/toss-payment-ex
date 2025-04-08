package com.yh.paymentwebflux.payment.application.port.`in`

data class CheckoutCommand(
    val cartId: Long,
    val productIds: List<Long>,
    val buyerId: Long,
    val idempotencyKey: String
)
