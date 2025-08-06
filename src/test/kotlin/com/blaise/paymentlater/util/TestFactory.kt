package com.blaise.paymentlater.util

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.extension.toMerchantRegisterResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.dto.request.*
import com.blaise.paymentlater.dto.shared.PaymentIntentFilterDto
import org.bson.types.ObjectId
import java.math.BigDecimal

object TestFactory {
    fun admin(
        id: ObjectId = ObjectId(),
        username: String = "admin1",
        password: String = "encodedPassword"
    ) = Admin(id, username, password)

    fun merchant(
        id: ObjectId = ObjectId("688343c2b89f9cf214b8aae5"),
        name: String = "merchant",
        email: String = "john@doe",
        webhookUrl: String = "https://example.com/webhook",
        isActive: Boolean = true,
        apiKey: String = "fake-api-key",
        apiKeyDigest: String = "fake-api-key-digest"
    ) = Merchant(id, name, email, apiKey, apiKeyDigest, webhookUrl, isActive)

    fun adminLoginRequestDto(username: String = "admin1", password: String = "password") =
        AdminLoginRequestDto(username, password)

    fun adminRegisterRequestDto(username: String = "admin1", password: String = "password") =
        AdminRegisterRequestDto(username, password)

    fun merchantRegisterRequestDto(
        name: String = "merchant",
        email: String = "john@doe",
        webhookUrl: String = "https://example.com/webhook"
    ) = MerchantRegisterRequestDto(
        name,
        email,
        webhookUrl
    )

    fun merchantResponseDto() = merchant().toMerchantRegisterResponseDto()

    fun paymentIntent1(): PaymentIntent {
        val billableItems = listOf(billableItem1(), billableItem2())
        val totalAmount = billableItems.sumOf { it.unitAmount * it.quantity.toBigDecimal() }

        return PaymentIntent(
            id = ObjectId("688343c2b89f9cf214b8aae5"),
            amount = totalAmount,
            currency = Currency.RWF,
            status = PaymentStatus.PENDING,
            metadata = paymentMetadataRequestDto(),
            merchantId = ObjectId(merchant().id.toHexString()),
            items = billableItems
        )
    }

    fun paymentIntent2() = PaymentIntent(
        id = ObjectId("699343c2b89f9cf214b8eeb5"),
        amount = BigDecimal.valueOf(99.9),
        currency = Currency.USD,
        status = PaymentStatus.FAILED,
        metadata = paymentMetadataRequestDto(),
        merchantId = ObjectId(merchant().id.toHexString()),
        items = listOf(billableItem1(), billableItem2())
    )

    fun billableItem1() = BillableItemRequestDto(
        name = "item1",
        unitAmount = BigDecimal.valueOf(10.0),
        description = "item1",
        quantity = 10
    )

    fun billableItem2() = BillableItemRequestDto(
        name = "item2",
        unitAmount = BigDecimal.valueOf(20.0),
        description = "item2",
        quantity = 20
    )

    fun paymentIntentRequestDto() = PaymentIntentRequestDto(
        currency = "RWF",
        metadata = paymentMetadataRequestDto(),
        items = listOf(billableItem1(), billableItem2())
    )

    fun paymentMetadataRequestDto() = PaymentMetadataRequestDto(
        referenceId = "ref1",
        userId = merchant().id.toHexString(),
        phone = "1234567890",
        email = "john@doe",
        description = "description1",
    )
}