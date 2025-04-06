package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import reactor.core.publisher.Mono

interface PaymentStatusUpdateRepository {
    fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean>
}