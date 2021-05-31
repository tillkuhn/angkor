package net.timafe.angkor

import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Event
import net.timafe.angkor.domain.enums.EventTopic
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.EventService
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.event.ContextStoppedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.util.*
import javax.annotation.PostConstruct


// @SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class))
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware",dateTimeProviderRef = "auditingDateTimeProvider")
@EnableConfigurationProperties(AppProperties::class)
@EnableCaching
class Application (
    private val env: Environment,
    private val eventService: EventService) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * https://javadeveloperzone.com/spring-boot/spring-boot-application-set-default-timezone/
     */
    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")) // It will set UTC timezone
        log.debug("Spring boot application running in UTC timezone :" + Date()) // It will print UTC timezone
    }

    @EventListener
    fun onStartup(event: ApplicationReadyEvent) {
        val appName = env.getProperty("spring.application.name")
        val msg = "[Startup] Service $appName is ready for business on port ${env.getProperty("server.port")}"
        eventService.publish(EventTopic.SYSTEM,
            Event(action = "startsvc:$appName", message = msg, userId = SecurityUtils.safeConvertToUUID(Constants.USER_SYSTEM))
        )
        log.info(msg)
    }

    @EventListener
    fun onShutdown(event: ContextStoppedEvent) {
        log.info("${env.getProperty("spring.application.name")} context was stopped")
    }

    companion object {
        /**
         * Main method which runs our app
         *
         * @param args the command line arguments.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }

}
