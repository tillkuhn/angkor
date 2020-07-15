package net.timafe.angkor.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration
class JacksonConfiguration {


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

    @Bean
    @Primary
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper? {
        val objectMapper = builder.build<ObjectMapper>()
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT,true)
        objectMapper.findAndRegisterModules()
        return objectMapper
    }
}
