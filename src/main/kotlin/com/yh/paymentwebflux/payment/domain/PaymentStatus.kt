package com.yh.paymentwebflux.payment.domain

enum class PaymentStatus(
    description: String,
) {
    NOT_STARTED("결제 시작 전"),
    EXECUTING("결제 진행 중"),
    SUCCESS("결제 승인 완료"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 상태 미확인");

    companion object {

        fun get(status: String): PaymentStatus {
            return entries.find { it.name == status} ?: throw IllegalArgumentException("Invalid payment status: $status")
        }
    }
}