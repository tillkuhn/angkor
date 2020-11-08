package net.timafe.angkor.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * Enable CORS for local development (profile dev)
 * We don't need it for prod since we proxy the backend via nginx
 * So frontend and backend share the same domain
 */
@Configuration
@EnableWebMvc
@Profile("!" + Constants.PROFILE_PROD)
class WebConfig : WebMvcConfigurer {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun addCorsMappings(registry: CorsRegistry) {
        log.trace("We've come for a good CORS")
        registry.addMapping(Constants.API_ROOT + "/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080", "http://localhost:4200")
                .allowedMethods("GET", "PUT", "POST", "DELETE", "OPTIONS")
    }
}
