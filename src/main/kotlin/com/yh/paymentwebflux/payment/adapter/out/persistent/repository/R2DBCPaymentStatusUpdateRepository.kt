package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import com.yh.paymentwebflux.payment.adapter.out.persistent.exception.PaymentAlreadyProceedException
import com.yh.paymentwebflux.payment.domain.PaymentStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2DBCPaymentStatusUpdateRepository(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : PaymentStatusUpdateRepository {
    override fun updatePaymentStatusToExecuting(
        orderId: String,
        paymentKey: String
    ): Mono<Boolean> {
        return checkPreviousPaymentOrderStatus(orderId)
            .flatMap { insertPaymentHistory(it, PaymentStatus.EXECUTING, "PAYMENT_CONFIRMATION_START") }
            .flatMap { updatePaymentOrderStatus(orderId, PaymentStatus.EXECUTING) }
            .flatMap { updatePaymentKey(orderId, paymentKey) }
            .`as`(transactionalOperator::transactional)
            .then(Mono.just(true))
    }



    // 현재 결제 상태를 조회해야함
    private fun checkPreviousPaymentOrderStatus(orderId: String): Mono<List<Pair<Long, String>>> {
        return selectPaymentOrderStatus(orderId)
            .handle { paymentOrder, sink ->
                when(paymentOrder.second) {
                    PaymentStatus.NOT_STARTED.name, PaymentStatus.UNKNOWN.name, PaymentStatus.EXECUTING.name -> {
                        sink.next(paymentOrder)
                    }
                    PaymentStatus.SUCCESS.name -> {
                        sink.error(PaymentAlreadyProceedException(message = "Payment already completed", status = PaymentStatus.SUCCESS))
                    }
                    PaymentStatus.FAILURE.name -> {
                        sink.error(PaymentAlreadyProceedException(message = "Payment already failed", status = PaymentStatus.FAILURE))
                    }
                }
            }
            .collectList()
    }

    private fun selectPaymentOrderStatus(orderId: String): Flux<Pair<Long, String>> {
        return databaseClient.sql(SELECT_PAYMENT_ORDER_STATUS)
            .bind("orderId", orderId)
            .fetch()
            .all()
            .map { Pair(it["id"] as Long, it["payment_order_status"] as String)}

    }

    private fun insertPaymentHistory(
        paymentOrderIdToStatus: List<Pair<Long, String>>,
        status: PaymentStatus,
        reason: String): Mono<Long> {
        if (paymentOrderIdToStatus.isEmpty()) return Mono.empty()

        val valuesClauses = paymentOrderIdToStatus.joinToString(", ") {
            "( ${it.first}, '${it.second}', '${status}', '${reason}' )"
        }

        return databaseClient.sql(INSERT_PAYMENT_HISTORY_QUERY(valuesClauses))
            .fetch()
            .rowsUpdated()
    }

    private fun updatePaymentOrderStatus(orderId: String, status: PaymentStatus): Mono<Long> {
        return databaseClient.sql(UPDATE_PAYMENT_ORDER_STATUS_QUERY)
            .bind("orderId", orderId)
            .bind("status", status)
            .fetch()
            .rowsUpdated()
    }

    private fun updatePaymentKey(orderId: String, paymentKey: String): Mono<Long> {
        return databaseClient.sql(UPDATE_PAYMENT_KEY_QUERY)
            .bind("orderId", orderId)
            .bind("paymentKey", paymentKey)
            .fetch()
            .rowsUpdated()
    }



    companion object {
        val SELECT_PAYMENT_ORDER_STATUS = """
            SELECT id, payment_order_status FROM payment_orders
            WHERE id = :orderId
        """.trimIndent()

        val INSERT_PAYMENT_HISTORY_QUERY = fun (valueClauses: String) = """
      INSERT INTO payment_order_histories (payment_order_id, previous_status, new_status, reason)
      VALUES $valueClauses
    """.trimIndent()

        val UPDATE_PAYMENT_ORDER_STATUS_QUERY = """
      UPDATE payment_orders
      SET payment_order_status = :status, updated_at = CURRENT_TIMESTAMP
      WHERE order_id = :orderId
    """.trimIndent()

        val UPDATE_PAYMENT_KEY_QUERY = """
      UPDATE payment_events 
      SET payment_key = :paymentKey
      WHERE order_id = :orderId
    """.trimIndent()

    }
}