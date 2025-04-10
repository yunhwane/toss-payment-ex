package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.domain.PaymentEventMessage
import reactor.core.publisher.Mono

interface PaymentOutboxRepository {
    fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage>
}