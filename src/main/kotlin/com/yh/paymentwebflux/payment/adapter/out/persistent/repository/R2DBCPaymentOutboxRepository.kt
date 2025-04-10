package com.yh.paymentwebflux.payment.adapter.out.persistent.repository

import com.yh.paymentwebflux.common.objectMapper
import com.yh.paymentwebflux.payment.adapter.out.stream.util.PartitionKeyUtil
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.domain.PaymentEventMessage
import com.yh.paymentwebflux.payment.domain.PaymentEventMessageType
import com.yh.paymentwebflux.payment.domain.PaymentStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono


@Repository
class R2DBCPaymentOutboxRepository(
    private val databaseClient: DatabaseClient,
    private val partitionKeyUtil: PartitionKeyUtil
) : PaymentOutboxRepository {
    override fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage> {
        require(command.status == PaymentStatus.SUCCESS)

        val paymentEventMessage = createPaymentEventMessage(command)

        return databaseClient.sql(INSERT_OUTBOX_QUERY)
            .bind("idempotencyKey", paymentEventMessage.payload["orderId"])
            .bind("partitionKey", paymentEventMessage.metadata["partitionKey"] ?: 0)
            .bind("type", paymentEventMessage.type)
            .bind("payload", objectMapper.writeValueAsString(paymentEventMessage.payload))
            .bind("metadata", objectMapper.writeValueAsString(paymentEventMessage.metadata))
            .fetch()
            .rowsUpdated()
            .thenReturn(paymentEventMessage)
    }

    private fun createPaymentEventMessage(command: PaymentStatusUpdateCommand): PaymentEventMessage {
        return PaymentEventMessage(
            type = PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS,
            payload = mapOf(
                "orderId" to command.orderId
            ),
            metadata = mapOf(
                "partitionKey" to partitionKeyUtil.createPartitionKey(command.orderId.hashCode())
            )
        )
    }


    companion object {
        val INSERT_OUTBOX_QUERY = """
            INSERT INTO payment_outboxes (idempotency_key, type, partition_key, payload, metadata) 
            VALUES (:idempotencyKey, :type, :partitionKey, :payload, :metadata)
        """.trimIndent()
    }
}