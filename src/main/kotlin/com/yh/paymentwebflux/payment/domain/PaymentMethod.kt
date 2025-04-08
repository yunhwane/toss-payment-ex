package com.yh.paymentwebflux.payment.domain

enum class PaymentMethod (
    val method: String,
){
    EASY_PAY("간편결제");


    companion object {
        fun get(method: String): PaymentMethod {
            return entries.find { it.method == method } ?: error("결제 수단 (method: $method) 는 올바르지 않은 수단입니다.")
        }
    }
}