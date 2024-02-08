package net.timafe.angkor.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * Enable CORS for local development (profile dev)
 * We don't need it for prod since we proxy the backend via nginx,
 * therefore frontend and backend share the same domain!
 */
@Configuration
@EnableWebMvc
@Profile("!" + Constants.PROFILE_PROD) // not prod
class WebConfig(private val objectMapper: ObjectMapper)  : WebMvcConfigurer{

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun addCorsMappings(registry: CorsRegistry) {
        log.trace("We've come for a good CORS")
        registry.addMapping(Constants.API_ROOT + "/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8080", "http://localhost:4200")
            .allowedMethods("GET", "PUT", "POST", "DELETE", "OPTIONS")
    }


    /**
     * CAUTION !!!
     * Without this line, EnableWebMvc prevents usage of our Primary Mapper defined in [JacksonConfig]
     #
     * https://stackoverflow.com/a/55958912/4292075
     * https://stackoverflow.com/a/49405364/4292075
     *
     * Similar issue with actuator/prometheus endpoint if there's no
     * HttpMessageConverter registered that could handle the "text/plain" MediaType, only
     * https://stackoverflow.com/a/52085616/4292075
     */
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {
        converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        converters.add( StringHttpMessageConverter())
        // addDefaultHttpMessageConverters(converters)
        super.configureMessageConverters(converters)
    }


}
