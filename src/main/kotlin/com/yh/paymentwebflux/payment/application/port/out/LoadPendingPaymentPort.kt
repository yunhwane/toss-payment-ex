package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.domain.PendingPaymentEvent
import reactor.core.publisher.Flux

interface LoadPendingPaymentPort {

    fun getPendingPayments(): Flux<PendingPaymentEvent>
}