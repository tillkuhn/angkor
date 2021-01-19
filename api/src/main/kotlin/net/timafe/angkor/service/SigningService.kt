package net.timafe.angkor.service

import net.timafe.angkor.config.AppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
class SigningService(private val appProperties: AppProperties) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    fun sign(input: String): String {
        log.debug("Signing $input")
        val message = String.format("%s%s",input,appProperties.apiToken)
        val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(message.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }
}
