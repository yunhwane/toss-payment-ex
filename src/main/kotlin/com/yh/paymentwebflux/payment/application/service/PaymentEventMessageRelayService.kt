package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.common.Logger
import com.yh.paymentwebflux.common.UseCase
import com.yh.paymentwebflux.payment.application.port.`in`.PaymentEventMessageRelayUseCase
import com.yh.paymentwebflux.payment.application.port.out.DispatchEventMessagePort
import com.yh.paymentwebflux.payment.application.port.out.LoadPendingPaymentEventMessagePort
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.scheduler.Schedulers

import java.util.concurrent.TimeUnit


@UseCase
class PaymentEventMessageRelayService(
    private val loadPendingPaymentEventMessagePort: LoadPendingPaymentEventMessagePort,
    private val dispatchEventMessagePort: DispatchEventMessagePort,
) : PaymentEventMessageRelayUseCase {

    private val scheduler = Schedulers.newSingle("payment-event-message-relay-scheduler")

    @Scheduled(fixedDelay = 180, initialDelay = 180, timeUnit = TimeUnit.SECONDS )
    override fun relay() {
        loadPendingPaymentEventMessagePort.getPendingPaymentEventMessage()
            .map { dispatchEventMessagePort.dispatch(it) }
            .onErrorContinue { err, _ -> Logger.error("messageRelay", err.message ?: "failed to relay message ", err) }
            .subscribeOn(scheduler)
            .subscribe()
    }
}