package net.timafe.angkor.config

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration
class JacksonConfig {

    /**
     * Support for Java date and time API.
     * @return the corresponding Jackson module.
     */
    @Bean
    fun javaTimeModule() = JavaTimeModule()

    @Bean
    fun jdk8TimeModule() = Jdk8Module()

    /*
     * Jackson Afterburner module to speed up serialization/deserialization.
     */
    @Bean
    fun afterburnerModule() = AfterburnerModule()

    // Seems to kick in only for testing ??
    @Bean
    @Primary
    fun customJson(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            // Also doesn't work :-(
            builder.indentOutput(true)
            // builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        }
    }
    /*
    @Bean
    @Primary
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper? {
        val objectMapper = builder.build<ObjectMapper>()
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        // objectMapper.configure(SerializationFeature.INDENT_OUTPUT,true)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        objectMapper.findAndRegisterModules()
        return objectMapper
    }*/
}
