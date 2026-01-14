package net.timafe.angkor.service

import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.security.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Basic External Authentication Service to secure API Calls from other Services / CLIs
 */
@Service
class ExternalAuthService(private val appProperties: AppProperties) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Returns and MessageDigest Signature of the input string (hex format)
     * using API Token as 2nd Element
     */
    fun signMessage(input: String): String {
        log.debug("Signing $input")
        val message = String.format("%s%s",input,appProperties.apiToken)
        return SecurityUtils.getMD5Digest(message)
    }

    /**
     * Validate the if content of API token header matches the apps api token
     * throws 403 ResponseStatusException if header is not present, or value is invalid
     *
     * @throws ResponseStatusException
     */
    fun validateApiToken(headers: HttpHeaders) {
        val userAgent = headers[HttpHeaders.USER_AGENT]
        val authHeader = headers[appProperties.apiTokenHeader]?.get(0)
        if (appProperties.apiToken != authHeader) {
            val msg = "Invalid or no ${appProperties.apiTokenHeader} set, value size=${authHeader?.length} userAgent=$userAgent "
            log.warn(msg)
            // check https://www.baeldung.com/spring-response-status-exception#1-generate-responsestatusexception
            // Produces e.g. {"timestamp":1611232822902,"status":403,"error":"Forbidden",
            // "message":"Invalid or no X-Auth-Token set, value size is null","path":"/api/v1/notes/reminders"}
            throw ResponseStatusException(HttpStatus.FORBIDDEN, msg)
        } else {
            log.trace("AuthHeader valid for userAgent={}", userAgent)
        }
    }
}
