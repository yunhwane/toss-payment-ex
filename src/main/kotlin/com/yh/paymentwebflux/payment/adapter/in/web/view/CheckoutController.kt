package com.yh.paymentwebflux.payment.adapter.`in`.web.view

import com.yh.paymentwebflux.common.IdempotencyCreator
import com.yh.paymentwebflux.common.WebAdapter
import com.yh.paymentwebflux.payment.adapter.`in`.web.request.CheckoutRequest
import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutCommand
import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutUseCase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono


@Controller
@WebAdapter
class CheckoutController (
    private val checkoutUseCase: CheckoutUseCase
){

    @GetMapping
    fun checkoutPage(request: CheckoutRequest, model: Model): Mono<String>  {
        val command = CheckoutCommand(
            cartId = request.certId,
            buyerId = request.buyerId,
            productIds = request.productIds,
            idempotencyKey = IdempotencyCreator.create(request.seed)
        )

        return checkoutUseCase.checkout(command)
            .map {
                model.addAttribute("orderId", it.orderId)
                model.addAttribute("orderName", it.orderName)
                model.addAttribute("amount", it.amount)
                "checkout"
            }
    }
}