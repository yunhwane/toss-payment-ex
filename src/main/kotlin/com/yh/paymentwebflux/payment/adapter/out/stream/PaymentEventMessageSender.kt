package com.yh.paymentwebflux.payment.adapter.out.stream

import com.yh.paymentwebflux.common.Logger
import com.yh.paymentwebflux.common.StreamAdapter
import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentOutboxRepository
import com.yh.paymentwebflux.payment.domain.PaymentEventMessage
import com.yh.paymentwebflux.payment.domain.PaymentEventMessageType
import jakarta.annotation.PostConstruct
import org.apache.logging.log4j.util.Supplier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.SenderResult


@Configuration
@StreamAdapter
class PaymentEventMessageSender(
    private val paymentOutboxRepository: PaymentOutboxRepository
) {


    private val sender = Sinks.many().unicast().onBackpressureBuffer<Message<PaymentEventMessage>>()
    private val sendResult = Sinks.many().unicast().onBackpressureBuffer<SenderResult<String>>()



    @Bean
    fun send(): Supplier<Flux<Message<PaymentEventMessage>>> {
        return Supplier {
            sender.asFlux()
                .onErrorContinue { err, _ ->
                    Logger.error("sendEventMessage",  err.message ?: "failed to send event message", err)
                }
        }
    }

    @Bean(name = ["payment-result"])
    fun sendResultChannel(): FluxMessageChannel {
        return FluxMessageChannel()
    }

    @ServiceActivator(inputChannel = "payment-result")
    fun receiveSendResult(results: SenderResult<String>) {
        if (results.exception() != null) {
           Logger.error("sendEventMessage", results.exception().message ?: "receive an exception for event message send ", results.exception())
        }

        sendResult.emitNext(results, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @PostConstruct
    fun handleSendResult(){
        sendResult.asFlux()
            .flatMap {
                when(it.recordMetadata() != null) {
                    true -> paymentOutboxRepository.markMessageAsSent(it.correlationMetadata(), PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS)
                    false -> paymentOutboxRepository.markMessageAsFailure(it.correlationMetadata(), PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS)
                }
            }
            .onErrorContinue {err, _ -> Logger.error("sendEventMessage", err.message ?: "failed to marked the outbox message.", err) }
            .subscribeOn(Schedulers.newSingle("handle-send-result-event-message"))
            .subscribe()
    }



    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun dispatchAfterCommit(paymentEventMessage: PaymentEventMessage) {
        dispatch(paymentEventMessage)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun dispatch(paymentEventMessage: PaymentEventMessage) {
        sender.emitNext(createEventMessage(paymentEventMessage), Sinks.EmitFailureHandler.FAIL_FAST)
    }

    private fun createEventMessage(paymentEventMessage: PaymentEventMessage): Message<PaymentEventMessage> {
        return MessageBuilder.withPayload(paymentEventMessage)
            .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, paymentEventMessage.payload["orderId"])
            .setHeader(KafkaHeaders.PARTITION, paymentEventMessage.metadata["partitionKey"] ?: 0)
            .build()
    }
}
