package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import reactor.core.publisher.Mono

interface PaymentValidationRepository {
    fun isValid(orderId: String, amount: Long): Mono<Boolean>
}