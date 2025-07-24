package com.blaise.paymentlater.util

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
    ) = Admin(id = id, username = username, password = password)

    fun merchant(
        id: ObjectId = ObjectId(),
        name: String = "merchant",
        email: String = "john@doe",
        webhookUrl: String = "https://example.com/webhook",
        isActive: Boolean = true,
        apiKey: String = "fake-api-key"
    ) = Merchant(id = id, name, email, apiKey, webhookUrl, isActive)

    fun adminLoginRequestDto(username: String = "admin1", password: String = "password") =
        AdminLoginRequestDto(username, password)

    fun adminRegisterRequestDto(username: String = "admin1", password: String = "password") =
        AdminRegisterRequestDto(username, password)

    fun merchantRegisterDto(
        name: String = "merchant",
        email: String = "john@doe",
        webhookUrl: String = "https://example.com/webhook"
    ) = MerchantRegisterRequestDto(
        name,
        email,
        webhookUrl
    )
}