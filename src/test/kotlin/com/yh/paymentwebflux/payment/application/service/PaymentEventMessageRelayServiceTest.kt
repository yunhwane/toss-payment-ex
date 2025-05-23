package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.payment.adapter.out.persistent.repository.PaymentOutboxRepository
import com.yh.paymentwebflux.payment.application.port.out.DispatchEventMessagePort
import com.yh.paymentwebflux.payment.application.port.out.LoadPendingPaymentEventMessagePort
import com.yh.paymentwebflux.payment.application.port.out.PaymentStatusUpdateCommand
import com.yh.paymentwebflux.payment.domain.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Hooks
import java.time.LocalDateTime
import java.util.*


@SpringBootTest
@Tag("ExternalIntegration")
class PaymentEventMessageRelayServiceTest (
    @Autowired private val paymentOutboxRepository: PaymentOutboxRepository,
    @Autowired private val loadPendingPaymentEventMessagePort: LoadPendingPaymentEventMessagePort,
    @Autowired private val dispatchEventMessagePort: DispatchEventMessagePort
) {

    @Test
    fun `should dispatch external message system`() {
        Hooks.onOperatorDebug()

        val paymentEventMessageRelayUseCase =  PaymentEventMessageRelayService(loadPendingPaymentEventMessagePort, dispatchEventMessagePort)

        val command = PaymentStatusUpdateCommand(
            paymentExecutionResult = PaymentExecutionResult(
                paymentKey = UUID.randomUUID().toString(),
                orderId = UUID.randomUUID().toString(),
                extraDetails = PaymentExtraDetails(
                    type = PaymentType.NORMAL,
                    method = PaymentMethod.EASY_PAY,
                    approvedAt = LocalDateTime.now(),
                    orderName = "test_order_name",
                    pspConfirmationStatus = PSPConfirmationStatus.DONE,
                    totalAmount = 50000L,
                    pspRawData = "{}"
                ),
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false
            )
        )

        paymentOutboxRepository.insertOutbox(command).block()

        paymentEventMessageRelayUseCase.relay()

        Thread.sleep(10000)
    }
}