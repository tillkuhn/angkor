package net.timafe.angkor

import net.timafe.angkor.config.Constants
import net.timafe.angkor.service.GeoService
import net.timafe.angkor.service.MockServices
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Special Mocks that should kick in during Integration Testing
 *
 * https://www.baeldung.com/injecting-mocks-in-spring
 */
@Profile(Constants.PROFILE_TEST)
@Configuration
class IntegrationTestsConfig {

    // Make sure we use mock geo service during Integration tests, not the real one that is rate limited
    @Bean
    // The @Primary annotation is there to make sure this instance is used instead of a real one for autowiring. 
    @Primary
    fun geoService(): GeoService {
        LoggerFactory.getLogger(this.javaClass).info("[Config] Using Mock GeoService in Integration Test")
        return MockServices.geoService()
    }

}
