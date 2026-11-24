package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.boot.session.autoconfigure.DefaultCookieSerializerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.serializer.support.DeserializingConverter
import org.springframework.core.serializer.support.SerializingConverter
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.jdbc.JdbcIndexedSessionRepository

/**
 * HTTP sessions are persisted in Postgres via Spring Session JDBC (see spring.session in application.yml),
 * so logins survive restarts and can be long-lived without holding them in memory.
 */
@Configuration
class SessionConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Keep OAuth2 authorized clients (access, ID and refresh token) in the (persisted) http session.
     * Spring Boot's default keeps them in an in-memory map, which is lost on restart and grows with every login.
     */
    @Bean
    fun authorizedClientRepository(): OAuth2AuthorizedClientRepository = HttpSessionOAuth2AuthorizedClientRepository()

    /**
     * Use the plain session ID as cookie value. Spring Session base64-decodes cookie values by default, so legacy
     * (Tomcat hex) or tampered JSESSIONID values decode to bytes like 0x00, which Postgres rejects with a 500 error
     * instead of simply not finding the session.
     */
    @Bean
    fun plainSessionCookie(): DefaultCookieSerializerCustomizer =
        DefaultCookieSerializerCustomizer { it.setUseBase64Encoding(false) }

    /**
     * Session attributes are stored as serialized java objects. If they can't be deserialized after an
     * upgrade (e.g. changed serialVersionUID of Spring Security classes), treat them as missing,
     * so the user simply has to log in again instead of running into server errors.
     */
    @Bean
    fun lenientSessionDeserialization(): SessionRepositoryCustomizer<JdbcIndexedSessionRepository> =
        SessionRepositoryCustomizer { repository ->
            val deserializer = DeserializingConverter(javaClass.classLoader)
            val conversionService = GenericConversionService()
            conversionService.addConverter(Any::class.java, ByteArray::class.java, SerializingConverter())
            conversionService.addConverter(object : GenericConverter {
                override fun getConvertibleTypes() = setOf(ConvertiblePair(ByteArray::class.java, Any::class.java))
                override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? =
                    try {
                        deserializer.convert(source as ByteArray)
                    } catch (e: Exception) {
                        log.warn("[Session] Ignoring session attribute that can't be deserialized: ${e.message}")
                        null
                    }
            })
            repository.setConversionService(conversionService)
        }
}
