package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.payment.adapter.out.persistent.exception.PaymentAlreadyProceedException
import com.yh.paymentwebflux.payment.adapter.out.persistent.exception.PaymentValidationException
import com.yh.paymentwebflux.payment.adapter.out.web.toss.exception.PSPConfirmationException
import com.yh.paymentwebflux.payment.application.port.`in`.PaymentConfirmCommand
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdatePort
import com.yh.paymentwebflux.payment.domain.PaymentConfirmationResult
import com.yh.paymentwebflux.payment.domain.PaymentExecutionFailure
import com.yh.paymentwebflux.payment.domain.PaymentStatus
import io.netty.handler.timeout.TimeoutException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class PaymentErrorHandler (
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort
) {

    fun handlePaymentConfirmationError(error: Throwable, command: PaymentConfirmCommand): Mono<PaymentConfirmationResult> {
        val (status, failure) = when (error) {
            is PSPConfirmationException -> Pair(error.paymentStatus(), PaymentExecutionFailure(error.errorCode, error.errorMessage))
            is PaymentValidationException -> Pair(PaymentStatus.FAILURE, PaymentExecutionFailure(error::class.simpleName ?: "", error.message ?: ""))
            is PaymentAlreadyProceedException -> return Mono.just(PaymentConfirmationResult(status = error.status, failure = PaymentExecutionFailure(message = error.message ?: "", errorCode = error::class.simpleName ?: "")))
            is TimeoutException -> Pair(PaymentStatus.UNKNOWN, PaymentExecutionFailure(error::class.simpleName ?: "", error.message ?: ""))
            else -> Pair(PaymentStatus.UNKNOWN, PaymentExecutionFailure(error::class.simpleName ?: "",  error.message ?: ""))
        }

        val paymentStatusUpdateCommand = PaymentStatusUpdateCommand(
            paymentKey = command.paymentKey,
            orderId = command.orderId,
            status = status,
            failure = failure
        )

        return paymentStatusUpdatePort.updatePaymentStatus(paymentStatusUpdateCommand)
            .map { PaymentConfirmationResult(status, failure) }
    }
}