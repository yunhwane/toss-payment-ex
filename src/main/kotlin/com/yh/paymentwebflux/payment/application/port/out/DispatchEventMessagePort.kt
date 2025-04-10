package com.yh.paymentwebflux.payment.application.port.out

import com.yh.paymentwebflux.payment.domain.PaymentEventMessage

interface DispatchEventMessagePort {
    fun dispatch(paymentEventMessage: PaymentEventMessage)
}