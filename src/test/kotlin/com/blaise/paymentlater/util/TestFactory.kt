package com.blaise.paymentlater.util

import com.blaise.paymentlater.domain.extension.toMerchantProfileResponseDto
import com.blaise.paymentlater.domain.extension.toMerchantRegisterResponseDto
import com.blaise.paymentlater.domain.model.Admin
import com.blaise.paymentlater.domain.model.Merchant
import com.blaise.paymentlater.dto.request.AdminLoginRequestDto
import com.blaise.paymentlater.dto.request.AdminRegisterRequestDto
import com.blaise.paymentlater.dto.request.MerchantRegisterRequestDto
import org.bson.types.ObjectId

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
}