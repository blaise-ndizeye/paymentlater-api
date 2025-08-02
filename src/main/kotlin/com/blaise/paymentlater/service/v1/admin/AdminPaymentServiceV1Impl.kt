package com.blaise.paymentlater.service.v1.admin

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toPaymentIntentResponseDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.repository.PaymentIntentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
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
        val pageable = PageRequest.of(
            page,
            size,
            Sort.Direction.DESC,
            "createdAt"
        )
        val paymentIntents = paymentIntentRepository.findByAdminFilters(
            statuses,
            currencies = currencies?.map { it.name },
            start,
            end,
            pageable
        )

        return paymentIntents.map {
            it.toPaymentIntentResponseDto()
        }.toPageResponseDto()
    }

}