package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.domain.Product
import reactor.core.publisher.Flux

interface LoadProductPort {
    fun getProducts(certId: Long, productIds: List<Long>): Flux<Product>
}