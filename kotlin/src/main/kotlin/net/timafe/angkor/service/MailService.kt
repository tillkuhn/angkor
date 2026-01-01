package net.timafe.angkor.service

import jakarta.annotation.PostConstruct
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.mail.autoconfigure.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Service

/** Mail Service see https://www.baeldung.com/spring-email */
@Service
class MailService(
    private val mailSender: JavaMailSender,
    private val mailProperties: MailProperties,
    @Value("\${app.admin-mail}")
    private val adminMail: String, // e.g. angkor-admin@domain.net"
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        log.info("[Mailer] Service initialized for smtp  ${mailProperties.username}@${mailProperties.host}:${mailProperties.properties["mail.smtp.port"]}")
        // prepareAndSend("hello", "Notification startup","Hello <b>world</b>")
    }

    fun prepareAndSend(recipient: String, subject: String, message: String) {
        // http://dolszewski.com/spring/sending-html-mail-with-spring-boot-and-thymeleaf/
        // Contains also greenmail example code
        val via = "mailProperties.host:${mailProperties.properties["mail.smtp.port"]}"
        val from = mailProperties.properties["mail.smtp.from"] ?: "admin@localhost"
        log.info("[Mailer] Trigger mail from $from via $via to $recipient bcc $adminMail")
        val composer = MimeMessagePreparator { mimeMessage: MimeMessage ->
            val messageHelper = MimeMessageHelper(mimeMessage, true)
            messageHelper.setFrom(from)
            messageHelper.setBcc(adminMail)
            messageHelper.setTo(recipient)

            messageHelper.setSubject(subject)
            messageHelper.setText(message, message) // first is plain, 2nd is html
        }
        mailSender.send(composer)
        log.info("[Mailer] Message successfully sent to $recipient")
    }

}
