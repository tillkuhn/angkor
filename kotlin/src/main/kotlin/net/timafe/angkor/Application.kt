package net.timafe.angkor

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.domain.enums.EventType
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.EventService
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootVersion
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.util.*

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@EnableConfigurationProperties(AppProperties::class)
@EnableCaching
class Application(
    private val env: Environment,
    private val eventService: EventService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Set default timezone - Same effect as passing user.timezone="xxx" with "java -d"
     * - https://javadeveloperzone.com/spring-boot/spring-boot-application-set-default-timezone/
     * - https://www.baeldung.com/java-jvm-time-zone
     */
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")) // It will set UTC timezone
        log.debug("[Config] Configured default UTC timezone at {}", Date()) // It will print UTC timezone
    }

    /**
     * Emit event on startup
     */
    @EventListener
    fun onStartup(event: ApplicationReadyEvent) {
        val appName = env.getProperty("spring.application.name")
        val msg = "Service $appName Spring Boot/${SpringBootVersion.getVersion()} Kotlin/${KotlinVersion.CURRENT} " +
                "Java ${System.getProperty("java.version")} is ready for business on port ${env.getProperty("server.port")} timeTake=${event.timeTaken}"
        val ev = Event(
            action = EventType.STARTUP.actionPrefix,
            message = msg,
            source = this.javaClass.simpleName,
            userId = SecurityUtils.safeConvertToUUID(Constants.USER_SYSTEM)
        )

        eventService.publish(
            EventTopic.SYSTEM,ev
        )
        log.info("[Ready] $msg")
    }

    @PreDestroy
    fun shutdown() {
        log.info("${env.getProperty("spring.application.name")} going down")
    }

    companion object {

        /**
         * Main method which bootstraps our app
         *
         * @param args the command line arguments.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }

}
