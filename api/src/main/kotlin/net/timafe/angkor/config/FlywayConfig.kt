package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * This config only kicks in if "clean" profile is uses,
 * will drop the entire schema and migrate from scratch
 */
@Configuration
class FlywayConfig {

    @Bean
    @Profile(Constants.PROFILE_CLEAN)
    fun cleanMigrateStrategy(): FlywayMigrationStrategy? {
        return FlywayMigrationStrategy { flyway ->
            LoggerFactory.getLogger(FlywayMigrationStrategy::class.java).info("Profile {}, cleaning Flyway Schema", Constants.PROFILE_CLEAN)
            flyway.clean()
            flyway.migrate()
        }
    }

}
