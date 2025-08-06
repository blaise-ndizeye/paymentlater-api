package com.blaise.paymentlater.service.v1.payment

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.extension.toPageResponseDto
import com.blaise.paymentlater.domain.extension.toPaymentIntentResponseDto
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.request.PaymentIntentRequestDto
import com.blaise.paymentlater.dto.response.PageResponseDto
import com.blaise.paymentlater.dto.response.PaymentIntentResponseDto
import com.blaise.paymentlater.repository.PaymentIntentRepository
import com.blaise.paymentlater.service.v1.merchant.MerchantAuthServiceV1
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant

private val log = KotlinLogging.logger {}

@Service
class PaymentServiceV1Impl(
    private val paymentIntentRepository: PaymentIntentRepository,
    private val merchantAuthService: MerchantAuthServiceV1
) : PaymentServiceV1 {

    override fun getPayments(
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
        }
            .toPageResponseDto()
            .also {
                log.info { "Found ${paymentIntents.totalElements} payment intents" }
            }
    }

    override fun createPaymentIntent(body: PaymentIntentRequestDto): PaymentIntentResponseDto {
        val merchant = merchantAuthService.getAuthenticatedMerchant()
        val totalAmount = body.items.sumOf { it.unitAmount * it.quantity.toBigDecimal() }

        return paymentIntentRepository.save(
            PaymentIntent(
                merchantId = merchant.id,
                items = body.items,
                amount = totalAmount,
                currency = Currency.valueOf(body.currency),
                metadata = body.metadata,
            )
        )
            .toPaymentIntentResponseDto()
            .also { log.info { "Created payment intent: ${it.id}" } }
    }
}