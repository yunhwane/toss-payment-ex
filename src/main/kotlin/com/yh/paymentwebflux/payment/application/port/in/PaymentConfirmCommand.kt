package com.yh.paymentwebflux.payment.application.port.`in`

data class PaymentConfirmCommand(
    val paymentKey: String,
    val orderId: String,
    val amount: Long
){
}