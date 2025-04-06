package com.yh.paymentwebflux.payment.adapter.out.persistent.exception

import com.yh.paymentwebflux.payment.domain.PaymentStatus

class PaymentAlreadyProceedException (
    val status: PaymentStatus,
    message: String
) : RuntimeException(message){
}