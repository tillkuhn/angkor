package net.timafe.angkor.service

import net.timafe.angkor.config.AppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest

@Service
class ExternalAuthService(private val appProperties: AppProperties) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val algorithm = "MD5" // "SHA-256"

    /**
     * Returns and MessageDigest Signature of the input string
     */
    fun signMessage(input: String): String {
        log.debug("Signing $input")
        val message = String.format("%s%s",input,appProperties.apiToken)
        val messageDigest: MessageDigest = MessageDigest.getInstance(algorithm)
        val digest = messageDigest.digest(message.toByteArray())
        // return Base64.getEncoder().encodeToString(digest)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validate the if content of API token header matches the apps api token
     * throws 403 ResponseStatusException if header is not present, or value is invalid
     *
     * @throws ResponseStatusException
     */
    fun validateApiToken(headers: HttpHeaders) {
        val authHeader = headers[appProperties.apiTokenHeader]?.get(0)
        if (appProperties.apiToken != authHeader) {
            val msg = "Invalid or no ${appProperties.apiTokenHeader} set, value size is ${authHeader?.length}"
            log.warn(msg)
            // check https://www.baeldung.com/spring-response-status-exception#1-generate-responsestatusexception
            // Produces e.g. {"timestamp":1611232822902,"status":403,"error":"Forbidden",
            // "message":"Invalid or no X-Auth-Token set, value size is null","path":"/api/v1/notes/reminders"}
            throw ResponseStatusException(HttpStatus.FORBIDDEN,msg)
        } else {
            log.trace("AuthHeader valid")
        }
    }
}
