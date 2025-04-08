package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.common.UseCase
import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmCommand
import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmUseCase
import com.yh.paymentwebflux.payment.application.port.out.PaymentExecutorPort
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdatePort
import com.yh.paymentwebflux.payment.application.port.out.PaymentValidationPort
import com.yh.paymentwebflux.payment.domain.PaymentConfirmationResult
import reactor.core.publisher.Mono


@UseCase
class PaymentConfirmService(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentExecutorPort: PaymentExecutorPort
) : PaymentConfirmUseCase{
    override fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult> {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId, command.paymentKey)
            .filterWhen { paymentValidationPort.isValid(command.orderId, command.amount) }
            .flatMap { paymentExecutorPort.execute(command) }
            .flatMap {
                paymentStatusUpdatePort.updatePaymentStatus(
                    command = PaymentStatusUpdateCommand(
                        paymentKey = it.paymentKey,
                        orderId = it.orderId,
                        status = it.paymentStatus(),
                        extraDetails = it.extraDetails,
                        failure = it.failure
                    )
                ).thenReturn(it)
            }
            .map { PaymentConfirmationResult(status = it.paymentStatus(), failure = it.failure) }
    }
}