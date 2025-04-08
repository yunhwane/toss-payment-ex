package com.yh.paymentwebflux.payment.adapter.out.web.toss.executor

import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmCommand
import com.yh.paymentwebflux.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono

interface PaymentExecutor {

    fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}