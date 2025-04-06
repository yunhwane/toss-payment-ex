package com.yh.paymentwebflux.payment.adapter.out.web.product

import com.yh.paymentwebflux.common.WebAdapter
import com.yh.paymentwebflux.payment.adapter.out.web.product.client.ProductClient
import com.yh.paymentwebflux.payment.application.port.out.LoadProductPort
import com.yh.paymentwebflux.payment.domain.Product
import reactor.core.publisher.Flux


@WebAdapter
class ProductWebAdapter(
    private val productClient: ProductClient
): LoadProductPort {

    override fun getProducts(
        certId: Long,
        productIds: List<Long>
    ): Flux<Product> {
        return productClient.getProducts(certId, productIds)
    }

}