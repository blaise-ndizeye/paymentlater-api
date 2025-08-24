package com.blaise.paymentlater.notification

import com.blaise.paymentlater.domain.enum.Currency
import com.blaise.paymentlater.domain.exception.EmailSendingException
import com.mongodb.MongoSocketReadTimeoutException
import com.mongodb.MongoTimeoutException
import jakarta.mail.MessagingException
import mu.KotlinLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal
import java.net.SocketTimeoutException

private val log = KotlinLogging.logger {}

@Service
class MailService(
    val javaMailSender: JavaMailSender,
    val templateEngine: TemplateEngine
) {

    @Retryable(
        value = [
            EmailSendingException::class,
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            SocketTimeoutException::class
        ],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000)
    )
    fun sendRegenerateApiKeyEmail(to: String, name: String, apiKey: String) {
        val htmlTemplate = "mail/merchant/regenerate-api-key.html"
        return sendApiKeyEmail(to, name, apiKey, htmlTemplate)
    }

    @Retryable(
        value = [
            EmailSendingException::class,
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            SocketTimeoutException::class
        ],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000)
    )
    fun sendMerchantRegisterApiKeyEmail(to: String, name: String, apiKey: String) {
        val htmlTemplate = "mail/merchant/merchant-register-api-key.html"
        return sendApiKeyEmail(to, name, apiKey, htmlTemplate)
    }

    @Retryable(
        value = [
            EmailSendingException::class,
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            SocketTimeoutException::class
        ],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000)
    )
    fun sendConfirmPaymentIntentEmail(
        to: String,
        name: String,
        status: String,
        amount: BigDecimal,
        currency: Currency,
        referenceId: String,
        description: String?
    ) {
        val htmlTemplate = "mail/payment/payment-confirmation.html"
        val context = Context().apply {
            setVariable("name", name)
            setVariable("status", status)
            setVariable("amount", amount)
            setVariable("currency", currency)
            setVariable("referenceId", referenceId)
            setVariable("description", description ?: "No description provided.")
        }
        val content = templateEngine.process(htmlTemplate, context)
        val message = javaMailSender.createMimeMessage()

        try {
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("Payment $status – PaymentLater")
            helper.setText(content, true)
            javaMailSender.send(message)
        } catch (e: MessagingException) {
            log.error("PAYMENT: Email message build failed for $to")
            throw EmailSendingException("Email message build failed for $to", e)
        } catch (e: MailException) {
            log.error("PAYMENT: Email sending failed to $to")
            throw EmailSendingException("Could not send the email", e)
        }
    }

    @Retryable(
        value = [
            EmailSendingException::class,
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            SocketTimeoutException::class
        ],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000)
    )
    fun sendRefundApprovedEmail(to: String, name: String, amount: BigDecimal, currency: Currency, reason: String) {
        val htmlTemplate = "mail/refund/refund-approved.html"
        return sendRefundEmail(to, name, amount, currency, reason, htmlTemplate)
    }

    @Retryable(
        value = [
            EmailSendingException::class,
            MongoTimeoutException::class,
            MongoSocketReadTimeoutException::class,
            SocketTimeoutException::class
        ],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000, multiplier = 2.0, maxDelay = 10000)
    )
    fun sendRefundRejectedEmail(
        to: String,
        name: String,
        amount: BigDecimal,
        currency: Currency,
        reason: String,
        rejectedReason: String
    ) {
        val htmlTemplate = "mail/refund/refund-rejected.html"
        return sendRefundEmail(to, name, amount, currency, reason, htmlTemplate, rejectedReason)
    }

    private fun sendApiKeyEmail(to: String, name: String, apiKey: String, htmlTemplate: String) {
        val context = Context().apply {
            setVariable("name", name)
            setVariable("apiKey", apiKey)
        }
        val content = templateEngine.process(htmlTemplate, context)
        val message = javaMailSender.createMimeMessage()

        try {
            val helper = MimeMessageHelper(message, true)

            helper.setTo(to)
            helper.setSubject("Your new API key – PaymentLater")
            helper.setText(content, true)

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            log.error("API-KEY: Email message build failed for $to")
            throw EmailSendingException("Email message build failed for $to", e)
        } catch (e: MailException) {
            log.error("API-KEY: Email sending failed to $to")
            throw EmailSendingException("Could not send the email", e)
        }
    }

    private fun sendRefundEmail(
        to: String,
        name: String,
        amount: BigDecimal,
        currency: Currency,
        reason: String,
        htmlTemplate: String,
        rejectedReason: String? = null,
    ) {
        val context = Context().apply {
            setVariable("name", name)
            setVariable("amount", amount)
            setVariable("currency", currency)
            setVariable("reason", reason)
            if (rejectedReason != null) {
                setVariable("rejectedReason", rejectedReason)
            }
        }
        val content = templateEngine.process(htmlTemplate, context)
        val message = javaMailSender.createMimeMessage()

        try {
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("Refund requested – PaymentLater")
            helper.setText(content, true)
            javaMailSender.send(message)
        } catch (e: MessagingException) {
            log.error("REFUND: Email message build failed for $to")
            throw EmailSendingException("Email message build failed for $to", e)
        } catch (e: MailException) {
            log.error("REFUND: Email sending failed to $to")
            throw EmailSendingException("Could not send the email", e)
        }
    }
}