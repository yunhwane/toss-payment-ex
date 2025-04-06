package com.yh.paymentwebflux.payment.domain

import java.math.BigDecimal

data class PaymentOrder(
    val id: Long? = null,
    val paymentEventId: Long? = null,
    val orderId : String,
    val sellerId: Long,
    val productId : Long,
    val buyerId : Long? = null,
    val amount: BigDecimal,
    val paymentStatus: PaymentStatus,
    private var isLedgerUpdated: Boolean = false,
    private var isWalletUpdated: Boolean = false,
) {
    fun isLedgerUpdated(): Boolean = isLedgerUpdated

    fun isWalletUpdated(): Boolean = isWalletUpdated
}
