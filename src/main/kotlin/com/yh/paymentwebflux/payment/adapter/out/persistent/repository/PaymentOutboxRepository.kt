package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.domain.PaymentEventMessage
import com.yh.paymentwebflux.payment.domain.PaymentEventMessageType
import reactor.core.publisher.Mono

interface PaymentOutboxRepository {
    fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage>
    fun markMessageAsSent(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>
    fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>
}