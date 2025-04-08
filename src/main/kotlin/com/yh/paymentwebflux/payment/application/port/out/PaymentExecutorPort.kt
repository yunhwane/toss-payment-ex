package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmCommand
import com.yh.paymentwebflux.payment.domain.PaymentConfirmationResult
import com.yh.paymentwebflux.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono

interface PaymentExecutorPort {

    fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}