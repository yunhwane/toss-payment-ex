package com.yh.paymentwebflux.payment.domain

data class CheckoutResult (
    val amount: Long,
    val orderId: String,
    val orderName: String
){
}