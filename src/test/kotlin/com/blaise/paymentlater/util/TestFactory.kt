package com.blaise.paymentlater.util

import com.blaise.paymentlater.domain.enums.Currency
import com.blaise.paymentlater.domain.enums.PaymentStatus
import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.extension.toMerchantRegisterResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.domain.model.PaymentIntent
import com.blaise.paymentlater.domain.model.sub.BillableItem
import com.blaise.paymentlater.domain.model.sub.PaymentMetadata
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import kotlin.text.get

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

    fun merchantProfileResponseDto() = merchant().toMerchantProfileResponseDto()

    fun merchantResponseDto() = merchant().toMerchantRegisterResponseDto()

    fun paymentIntent1() = PaymentIntent(
        id = ObjectId("688343c2b89f9cf214b8aae5"),
        amount = BigDecimal.valueOf(100.0),
        currency = Currency.RWF,
        status = PaymentStatus.PENDING,
        metadata = PaymentMetadata(
            referenceId = "ref1",
            userId = merchant().id.toHexString(),
            phone = "1234567890",
            email = "john@doe",
            description = "description1",
        ),
        merchantId = ObjectId(merchant().id.toHexString()),
        items = listOf(billableItem())
    )

    fun paymentIntent2() = PaymentIntent(
        id = ObjectId("699343c2b89f9cf214b8eeb5"),
        amount = BigDecimal.valueOf(99.9),
        currency = Currency.USD,
        status = PaymentStatus.FAILED,
        metadata = PaymentMetadata(
            referenceId = "ref2",
            userId = merchant().id.toHexString(),
            phone = "1234567888",
            email = "mercy@doe",
            description = "description2",
        ),
        merchantId = ObjectId(merchant().id.toHexString()),
        items = listOf(billableItem())
    )

    fun billableItem() = BillableItem(
        id ="688343c2b89f9cf214b8aae5",
        name = "item1",
        unitAmount = BigDecimal.valueOf(10.0),
        description = "item1",
        quantity = 10
    )
}