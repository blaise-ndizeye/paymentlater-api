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

/**
 * Email notification service for PaymentLater API.
 * 
 * Handles all email communications including:
 * - API key delivery (registration and regeneration)
 * - Payment confirmation notifications
 * - Refund status notifications (approved/rejected)
 * 
 * Features:
 * - HTML email templates using Thymeleaf
 * - Automatic retry mechanism with exponential backoff
 * - Comprehensive error handling and logging
 * - Support for various email types with consistent branding
 * 
 * All email methods are decorated with @Retryable to handle transient failures
 * such as network timeouts, mail server issues, and database connectivity problems.
 * 
 * @property javaMailSender Spring Mail sender for SMTP operations
 * @property templateEngine Thymeleaf engine for HTML template processing
 */
@Service
class MailService(
    val javaMailSender: JavaMailSender,
    val templateEngine: TemplateEngine
) {

    /**
     * Send API key regeneration email to merchant.
     * 
     * Sends a professionally formatted email containing the merchant's newly regenerated
     * API key using the regenerate-api-key HTML template. This is typically triggered
     * when a merchant requests a new API key for security reasons.
     * 
     * @param to Merchant's email address
     * @param name Merchant's display name for personalization
     * @param apiKey The newly generated API key to include in email
     * 
     * @throws EmailSendingException if email fails to send after all retry attempts
     * @throws MessagingException if email message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Send welcome email with API key to newly registered merchant.
     * 
     * Sends a welcome email containing the merchant's initial API key using the
     * merchant-register-api-key HTML template. This is sent immediately after
     * successful merchant registration to provide integration credentials.
     * 
     * @param to New merchant's email address
     * @param name Merchant's display name for personalization
     * @param apiKey The initial API key for integration
     * 
     * @throws EmailSendingException if email fails to send after all retry attempts
     * @throws MessagingException if email message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Send payment confirmation email to merchant.
     * 
     * Notifies merchant about payment status changes using the payment-confirmation
     * HTML template. Includes comprehensive payment details and transaction reference
     * for merchant's records and reconciliation.
     * 
     * @param to Merchant's email address
     * @param name Merchant's display name for personalization
     * @param status Payment status (SUCCESS, FAILED, etc.)
     * @param amount Transaction amount
     * @param currency Transaction currency (USD, EUR, RWF)
     * @param referenceId Unique payment reference identifier
     * @param description Optional payment description, defaults to "No description provided."
     * 
     * @throws EmailSendingException if email fails to send after all retry attempts
     * @throws MessagingException if email message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Send refund approval notification email to merchant.
     * 
     * Notifies merchant that their refund request has been approved using the
     * refund-approved HTML template. Contains refund details for merchant records.
     * 
     * @param to Merchant's email address
     * @param name Merchant's display name for personalization
     * @param amount Refunded amount
     * @param currency Refund currency (USD, EUR, RWF)
     * @param reason Original refund request reason
     * 
     * @throws EmailSendingException if email fails to send after all retry attempts
     * @throws MessagingException if email message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Send refund rejection notification email to merchant.
     * 
     * Notifies merchant that their refund request has been rejected using the
     * refund-rejected HTML template. Includes both the original reason and
     * the administrator's rejection explanation for transparency.
     * 
     * @param to Merchant's email address
     * @param name Merchant's display name for personalization
     * @param amount Requested refund amount
     * @param currency Refund currency (USD, EUR, RWF)
     * @param reason Original refund request reason
     * @param rejectedReason Administrator's explanation for rejection
     * 
     * @throws EmailSendingException if email fails to send after all retry attempts
     * @throws MessagingException if email message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Private helper method to send API key emails with specified template.
     * 
     * Common implementation for both registration and regeneration API key emails.
     * Handles template processing, message construction, and error handling.
     * 
     * @param to Recipient email address
     * @param name Recipient name for personalization
     * @param apiKey API key to include in email
     * @param htmlTemplate Thymeleaf template path
     * 
     * @throws EmailSendingException if email fails to send
     * @throws MessagingException if message construction fails
     * @throws MailException if SMTP delivery fails
     */
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

    /**
     * Private helper method to send refund status emails with specified template.
     * 
     * Common implementation for both approved and rejected refund notifications.
     * Handles template processing with refund-specific variables, message construction,
     * and error handling. Supports optional rejection reason for rejected refunds.
     * 
     * @param to Recipient email address
     * @param name Recipient name for personalization
     * @param amount Refund amount
     * @param currency Refund currency
     * @param reason Original refund request reason
     * @param htmlTemplate Thymeleaf template path
     * @param rejectedReason Optional rejection explanation (for rejected refunds only)
     * 
     * @throws EmailSendingException if email fails to send
     * @throws MessagingException if message construction fails
     * @throws MailException if SMTP delivery fails
     */
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