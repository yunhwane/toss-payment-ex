package com.yh.paymentwebflux.payment.adapter.out.web.product.client

import com.yh.paymentwebflux.payment.domain.Product
import reactor.core.publisher.Flux


interface ProductClient {
    fun getProducts(certId: Long, productIds: List<Long>): Flux<Product>
}