package com.yh.paymentwebflux.payment.adapter.out.web.toss

import com.yh.paymentwebflux.common.WebAdapter
import com.yh.paymentwebflux.payment.adapter.out.web.toss.executor.PaymentExecutor
import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmCommand
import com.yh.paymentwebflux.payment.application.port.out.PaymentExecutorPort
import com.yh.paymentwebflux.payment.domain.PaymentExecutionResult
import reactor.core.publisher.Mono


@WebAdapter
class PaymentExecutorWebAdapter(
    private val paymentExecutor: PaymentExecutor
) : PaymentExecutorPort{
    override fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult> {
        return paymentExecutor.execute(command)
    }
}