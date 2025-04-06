package com.yh.paymentwebflux.payment.domain

enum class PaymentMethod (
    description: String,
){
    EASY_PAY("간편 결제"),
    CARD("신용 카드"),
}