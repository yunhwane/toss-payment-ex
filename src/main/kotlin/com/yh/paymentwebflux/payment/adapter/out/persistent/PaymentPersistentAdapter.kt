package com.yh.paymentwebflux.payment.adapter.out.persistent

import com.yh.paymentwebflux.common.PersistentAdapter
import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentRepository
import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentStatusUpdateRepository
import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentValidationRepository
import com.yh.paymentwebflux.payment.application.port.out.*
import com.yh.paymentwebflux.payment.domain.PaymentEvent
import com.yh.paymentwebflux.payment.domain.PendingPaymentEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusUpdateRepository: PaymentStatusUpdateRepository,
    private val paymentValidationRepository: PaymentValidationRepository,
) : SavePaymentPort, PaymentStatusUpdatePort, PaymentValidationPort, LoadPendingPaymentPort {

    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }

    override fun updatePaymentStatusToExecuting(
        orderId: String,
        paymentKey: String
    ): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId, paymentKey)
    }

    override fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatus(command)
    }

    override fun isValid(orderId: String, amount: Long): Mono<Boolean> {
        return paymentValidationRepository.isValid(orderId, amount)
    }

    override fun getPendingPayments(): Flux<PendingPaymentEvent> {
        return paymentRepository.getPendingPayments()
    }
}