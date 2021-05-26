package net.timafe.angkor

import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.domain.dto.EventMessage
import net.timafe.angkor.domain.enums.EventTopic
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

// @SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class))
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
@EnableConfigurationProperties(AppProperties::class)
@EnableCaching
class Application (
    private val env: Environment,
    private val eventService: EventService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onStartup(event: ApplicationReadyEvent) {
        val appName = env.getProperty("spring.application.name")
        val msg = "$appName is ready for business on port ${env.getProperty("server.port")}"
        eventService.publish(EventTopic.SYSTEM, EventMessage(action = "startsvc:$appName", message = msg))
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
