package net.timafe.angkor.repo

import org.springframework.boot.actuate.health.HealthContributor
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component

import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

/**
 * https://reflectoring.io/spring-boot-health-check/
 *
 * $ curl localhost:8080/actuator/health
 * {"status":"UP","components":{"database":{"status":"UP"},"db":{"status":"UP",
 * "details":{"database":"PostgreSQL","validationQuery":"isValid()"}}, (...)
 */
@Component
class DatabaseHealthContributor(
    private val ds: DataSource

): HealthIndicator, HealthContributor {

    override fun health(): Health? {
        try {
            ds.connection.use { conn ->
                val stmt: Statement = conn.createStatement()
                stmt.execute("select count(*) from place")
            }
        } catch (ex: SQLException) {
            return Health.outOfService().withException(ex).build()
        }
        return Health.up().build()
    }
}
