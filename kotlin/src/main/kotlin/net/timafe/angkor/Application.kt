package net.timafe.angkor

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
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


// @SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class))
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@EnableConfigurationProperties(AppProperties::class)
@EnableCaching
class Application(
    private val env: Environment,
    private val eventService: EventService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Same effect as java -Duser.timezone="xxx"
     * https://javadeveloperzone.com/spring-boot/spring-boot-application-set-default-timezone/
     * https://www.baeldung.com/java-jvm-time-zone
     */
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")) // It will set UTC timezone
        log.debug("Configured default UTC timezone at ${Date()}") // It will print UTC timezone
    }

    @EventListener
    fun onStartup(event: ApplicationReadyEvent) {
        val appName = env.getProperty("spring.application.name")
        val msg = "Service $appName running Spring Boot ${SpringBootVersion.getVersion()} " +
                "Java ${System.getProperty("java.version")} is ready for business on port ${env.getProperty("server.port")}"
        eventService.publish(
            EventTopic.SYSTEM,
            Event(
                action = "${EventType.STARTUP.actionPrefix}:$appName",
                message = msg,
                userId = SecurityUtils.safeConvertToUUID(Constants.USER_SYSTEM)
            )
        )
        log.info(msg)
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
