package com.blaise.paymentlater.notification

import com.blaise.paymentlater.domain.exception.EmailSendingException
import jakarta.mail.MessagingException
import mu.KotlinLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

private val log = KotlinLogging.logger {}

@Service
class MailService(
    val javaMailSender: JavaMailSender,
    val templateEngine: TemplateEngine
) {
    fun sendApiKeyEmail(to: String, name: String, apiKey: String) {
        val context = Context().apply {
            setVariable("name", name)
            setVariable("apiKey", apiKey)
        }
        val content = templateEngine.process("mail/merchant/regenerate-api-key.html", context)
        val message = javaMailSender.createMimeMessage()

        try {
            val helper = MimeMessageHelper(message, true)

            helper.setTo(to)
            helper.setSubject("Your new API key – PaymentLater")
            helper.setText(content, true)

            javaMailSender.send(message)
        } catch (e: MessagingException) {
            log.error("Email message build failed for $to")
            throw EmailSendingException("Email message build failed for $to", e)
        } catch (e: MailException) {
            log.error("Email sending failed to $to")
            throw EmailSendingException("Could not send the email", e)
        }
    }
}