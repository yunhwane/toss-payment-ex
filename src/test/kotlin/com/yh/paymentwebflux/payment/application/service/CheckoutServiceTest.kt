package com.yh.paymentwebflux.payment.application.service

import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutCommand
import com.yh.paymentwebflux.payment.application.port.`in`.CheckoutUseCase
import com.yh.paymentwebflux.payment.test.PaymentDatabaseHelper
import com.yh.paymentwebflux.payment.test.PaymentTestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import reactor.test.StepVerifier
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

@SpringBootTest
@Import(PaymentTestConfiguration::class)
class CheckoutServiceTest(
    @Autowired private val checkoutUseCase: CheckoutUseCase,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper
) {

    @BeforeEach
    fun setUp() {
        paymentDatabaseHelper.clean()
    }

    @Test
    fun `should save PaymentEvent and PaymentOrder successfully`() {
        val orderId = UUID.randomUUID().toString()
        val checkoutCommand = CheckoutCommand(
            certId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        StepVerifier.create(checkoutUseCase.checkout(checkoutCommand))
            .expectNextMatches {
                it.amount.toInt() == 60000 && it.orderId == orderId
            }
            .verifyComplete()

        val paymentEvent = paymentDatabaseHelper.getPayments(orderId)!!

        assertThat(paymentEvent.orderId).isEqualTo(orderId)
        assertThat(paymentEvent.totalAmount()).isEqualTo(60000)
        assertThat(paymentEvent.paymentOrders.size).isEqualTo(checkoutCommand.productIds.size)
        assertFalse(paymentEvent.isPaymentDone())
        assertTrue(paymentEvent.paymentOrders.all { !it.isLedgerUpdated() })
        assertTrue(paymentEvent.paymentOrders.all { !it.isWalletUpdated() })
    }

    @Test
    fun `should fail to save PaymentEvent and PaymentOrder when trying to save for the second time`() {
        val orderId = UUID.randomUUID().toString()
        val checkoutCommand = CheckoutCommand(
            certId = 1,
            buyerId = 1,
            productIds = listOf(1, 2, 3),
            idempotencyKey = orderId
        )

        checkoutUseCase.checkout(checkoutCommand).block()

        assertThrows<DataIntegrityViolationException> {
            checkoutUseCase.checkout(checkoutCommand).block()
        }
    }
}