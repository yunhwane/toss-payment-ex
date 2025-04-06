package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.common.UseCase
import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutCommand
import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutUseCase
import com.yh.paymentwebflux.payment.application.port.out.LoadProductPort
import com.yh.paymentwebflux.payment.application.port.out.SavePaymentPort
import com.yh.paymentwebflux.payment.domain.CheckoutResult
import com.yh.paymentwebflux.payment.domain.PaymentEvent
import com.yh.paymentwebflux.payment.domain.PaymentOrder
import com.yh.paymentwebflux.payment.domain.PaymentStatus
import com.yh.paymentwebflux.payment.domain.Product
import reactor.core.publisher.Mono


@UseCase
class CheckoutService(
    private val loadProductPort: LoadProductPort,
    private val savePaymentPort: SavePaymentPort,
) : CheckoutUseCase{

    override fun checkout(command: CheckoutCommand): Mono<CheckoutResult> {
        return loadProductPort.getProducts(command.certId, command.productIds)
            .collectList()
            .map { createPaymentEvent(command, it)}
            .flatMap { savePaymentPort.save(it).thenReturn(it) }
            .map { CheckoutResult(
                amount = it.totalAmount(),
                orderId = it.orderId,
                orderName = it.orderName
            ) }
    }

    private fun createPaymentEvent(command: CheckoutCommand, products: List<Product>): PaymentEvent {
        return PaymentEvent(
            buyerId = command.buyerId,
            orderId = command.idempotencyKey,
            orderName = products.joinToString { it.name },
            paymentOrders = products.map {
                PaymentOrder(
                    sellerId = it.sellerId,
                    orderId = command.idempotencyKey,
                    productId = it.id,
                    amount = it.amount,
                    paymentStatus = PaymentStatus.NOT_STARTED
                )
            }
        )
    }

}