package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.enum.PaymentStatus
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toPaymentIntentResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import com.blaise.paymentlater.repository.PaymentIntentRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AdminPaymentServiceV1Impl(
    private val paymentIntentRepository: PaymentIntentRepository
) : AdminPaymentServiceV1 {

    override fun search(
        statuses: List<PaymentStatus>?,
        currencies: List<Currency>?,
        start: Instant?,
        end: Instant?,
        page: Int,
        size: Int
    ): PageResponseDto<PaymentIntentResponseDto> {
        val filter = PaymentIntentFilterDto(
            statuses = statuses,
            currencies = currencies,
            start = start,
            end = end
        )
        val paymentIntents = paymentIntentRepository.search(
            filter,
            page,
            size
        )

        return paymentIntents.map {
            it.toPaymentIntentResponseDto()
        }.toPageResponseDto()
    }

}