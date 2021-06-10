package net.timafe.angkor.config

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

    /**
     * This should configure the output, but it seems to kick in only for testing ??
     * Same with  objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
     * and objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
     */
    @Bean
    @Primary
    fun customJson(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder.indentOutput(true)
        }
    }

}
