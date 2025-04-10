package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.domain.PaymentEventMessage
import reactor.core.publisher.Flux

interface LoadPendingPaymentEventMessagePort {
    fun getPendingPaymentEventMessage(): Flux<PaymentEventMessage>
}