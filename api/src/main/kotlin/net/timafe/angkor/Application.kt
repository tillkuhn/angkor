package net.timafe.angkor

import net.timafe.angkor.config.AppProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// @SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class))
@SpringBootApplication
@EnableJpaRepositories
@EnableConfigurationProperties(AppProperties::class)
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

