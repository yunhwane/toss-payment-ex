package com.yh.paymentwebflux.payment.application.port.`in`

import com.yh.paymentwebflux.payment.domain.CheckoutResult
import reactor.core.publisher.Mono


interface CheckoutUseCase {

    fun checkout(command: CheckoutCommand): Mono<CheckoutResult>
}