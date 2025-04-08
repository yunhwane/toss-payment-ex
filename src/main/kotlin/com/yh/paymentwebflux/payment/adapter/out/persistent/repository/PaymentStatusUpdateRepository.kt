package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import reactor.core.publisher.Mono

interface PaymentStatusUpdateRepository {
    fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean>

    fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean>
}