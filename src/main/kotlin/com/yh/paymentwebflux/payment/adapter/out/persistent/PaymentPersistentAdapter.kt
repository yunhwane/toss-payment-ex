package com.yh.paymentwebflux.payment.adapter.out.persistent

import com.yh.paymentwebflux.common.PersistentAdapter
import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentRepository
import com.yh.paymentwebflux.payment.application.port.out.SavePaymentPort
import com.yh.paymentwebflux.payment.domain.PaymentEvent
import reactor.core.publisher.Mono


@PersistentAdapter
class PaymentPersistentAdapter(
    private val paymentRepository: PaymentRepository
) : SavePaymentPort{

    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }
}