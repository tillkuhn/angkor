package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * DANGER !!! DANGER !!! DANGER !!!
 *
 * This configuration only kicks in if the profile "clean" is present,
 * and will drop the entire schema to migrate schema and data from scratch
 */
@Configuration
class FlywayConfig {

    @Bean
    @Profile(Constants.PROFILE_CLEAN)
    fun cleanMigrateStrategy(): FlywayMigrationStrategy? {
        return FlywayMigrationStrategy { flyway ->
            LoggerFactory.getLogger(javaClass).info("Profile {}, cleaning Flyway Schema", Constants.PROFILE_CLEAN)
            flyway.clean() // drops all objects in the configured schemas
            flyway.migrate() // starts the migration all over again
        }
    }

}
