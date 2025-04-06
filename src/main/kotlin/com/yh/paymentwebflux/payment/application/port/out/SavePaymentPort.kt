package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.domain.PaymentEvent
import reactor.core.publisher.Mono

interface SavePaymentPort {

    fun save(paymentEvent: PaymentEvent): Mono<Void>
}