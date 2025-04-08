package com.yh.paymentwebflux.payment.test

import com.yh.paymentwebflux.payment.domain.PaymentEvent
import com.yh.paymentwebflux.payment.domain.PaymentMethod
import com.yh.paymentwebflux.payment.domain.PaymentOrder
import com.yh.paymentwebflux.payment.domain.PaymentStatus
import com.yh.paymentwebflux.payment.domain.PaymentType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.text.get


class R2DBCPaymentDatabaseHelper(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : PaymentDatabaseHelper {
    override fun getPayments(orderId: String): PaymentEvent? {
        return databaseClient.sql(SELECT_PAYMENT_QUERY)
            .bind("orderId", orderId)
            .fetch()
            .all()
            .groupBy { it["payment_event_id"] as Long }
            .flatMap { groupedFlux ->
                groupedFlux.collectList().map {
                        results ->
                    PaymentEvent(
                        id = groupedFlux.key(),
                        orderId = results.first()["order_id"] as String,
                        orderName = results.first()["order_name"] as String,
                        buyerId = results.first()["buyer_id"] as Long,
                        paymentKey = results.first()["payment_key"] as String?,
                        paymentType = if (results.first()["type"] != null) PaymentType.get(results.first()["type"] as String) else null,
                        paymentMethod = if (results.first()["method"] != null) PaymentMethod.valueOf(results.first()["method"] as String) else null,
                        approvedAt = if (results.first()["approved_at"] != null) (results.first()["approved_at"] as LocalDateTime) else null,
                        isPaymentDone = ((results.first()["is_payment_done"] as Byte).toInt() == 1),
                        paymentOrders = results.map { result ->
                            PaymentOrder(
                                id = result["id"] as Long,
                                paymentEventId = groupedFlux.key(),
                                sellerId = result["seller_id"] as Long,
                                orderId = result["order_id"] as String,
                                productId = result["product_id"] as Long,
                                amount = (result["amount"] as BigDecimal).toLong(),
                                paymentStatus = PaymentStatus.get(result["payment_order_status"] as String),
                                isLedgerUpdated = ((result["ledger_updated"]) as Byte).toInt() == 1,
                                isWalletUpdated = ((result["wallet_updated"]) as Byte).toInt() == 1
                            )
                        }
                    )
                }
            }.toMono().block()
    }

    override fun clean(): Mono<Void> {
        return deletePaymentOrderHistory()
            .flatMap { deletePaymentOrders() }
            .flatMap { deletePaymentEvents() }
            .`as`(transactionalOperator::transactional)
            .then()
    }

    private fun deletePaymentOrders(): Mono<Long> {
        return databaseClient.sql(DELETE_PAYMENT_ORDER_QUERY)
            .fetch()
            .rowsUpdated()
    }

    private fun deletePaymentEvents(): Mono<Long> {
        return databaseClient.sql(DELETE_PAYMENT_EVENT_QUERY)
            .fetch()
            .rowsUpdated()
    }

    private fun deletePaymentOrderHistory(): Mono<Long> {
        return databaseClient.sql(DELETE_PAYMENT_ORDER_HISTORY_QUERY)
            .fetch()
            .rowsUpdated()
    }


    companion object {
        val SELECT_PAYMENT_QUERY = """
            SELECT * FROM payment_events pe
            INNER JOIN payment_orders po on pe.order_id = po.order_id
            WHERE pe.order_id = :orderId
        """.trimIndent()

        val DELETE_PAYMENT_EVENT_QUERY = """
            DELETE FROM payment_events
        """.trimIndent()

        val DELETE_PAYMENT_ORDER_QUERY = """
            DELETE FROM payment_orders
        """.trimIndent()

        val DELETE_PAYMENT_ORDER_HISTORY_QUERY = """
            DELETE FROM payment_order_histories
        """.trimIndent()
    }
}