package com.blaise.paymentlater.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "PaymentLater API",
        version = "v1",
        description = "API for PaymentLater"
    ),
    security = [SecurityRequirement(name = "Bearer"), SecurityRequirement(name = "X-API-KEY")]
)
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .components(
            Components()
                .addSecuritySchemes(
                    "BearerToken", SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(
                    "ApiKey", SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name("X-API-KEY")
                )
        )
        .addSecurityItem(
            io.swagger.v3.oas.models.security.SecurityRequirement()
                .addList("ApiKey")
                .addList("BearerToken")
        )
}