package com.yh.paymentwebflux.payment.test

import com.yh.paymentwebflux.payment.domain.PaymentEvent
import reactor.core.publisher.Mono

interface PaymentDatabaseHelper {
    fun getPayments(orderId: String): PaymentEvent?
    fun clean(): Mono<Void>
}