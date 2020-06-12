package org.timafe.p2b

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// @SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class))
@SpringBootApplication
@EnableJpaRepositories
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
