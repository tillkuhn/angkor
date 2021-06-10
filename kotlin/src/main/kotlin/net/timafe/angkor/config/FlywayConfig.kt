package net.timafe.angkor.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * CAUTION !!!
 * This config only kicks in if the "clean" profile is used,
 * and will drop the entire schema to migrate from scratch
 */
@Configuration
class FlywayConfig {

    @Bean
    @Profile(Constants.PROFILE_CLEAN)
    fun cleanMigrateStrategy(): FlywayMigrationStrategy? {
        return FlywayMigrationStrategy { flyway ->
            LoggerFactory.getLogger(FlywayMigrationStrategy::class.java)
                .info("Profile {}, cleaning Flyway Schema", Constants.PROFILE_CLEAN)
            flyway.clean()
            flyway.migrate()
        }
    }

}
