package com.yh.paymentwebflux.payment.application.port.`in`

import com.yh.paymentwebflux.payment.domain.PaymentConfirmationResult
import reactor.core.publisher.Mono

interface PaymentConfirmUseCase {
    fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult>
}