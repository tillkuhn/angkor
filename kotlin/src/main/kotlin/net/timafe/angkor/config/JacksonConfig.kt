package net.timafe.angkor.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


/**
 * Configure Jackson JSON Serialization
 */
@Configuration
class JacksonConfig {

    /**
     * CAUTION: Make sure EnableWebMvc does also use *this* mapper, see [WebConfig] Configuration
     * - https://www.baeldung.com/spring-boot-customize-jackson-objectmapper
     * - https://stackoverflow.com/questions/55811031/is-it-possible-to-simplify-jsonserialize-annotations
     * - https://codingnconcepts.com/spring-boot/jackson-json-request-response-mapping
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val om = ObjectMapper()

        /* Modules */

        // The @JsonFormat annotation on properties for local dates is now obsolete, hopefully
        // See https://stackoverflow.com/a/60547263/4292075
        om.registerModule(JavaTimeModule())
        om.registerModule(Jdk8Module())
        // wee need this, or we get null values during serialization for props not present in json
        // https://github.com/FasterXML/jackson-module-kotlin/issues/177
        // https://github.com/FasterXML/jackson-module-kotlin/issues/130
        om.registerModule(KotlinModule())

        /*  toJson()  Serialization Features */
        // important to get 2020-07-21T14:33:31.407Z format
        // To get even more control, maybe we can add serializer
        // LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.JACKSON_DATE_TIME_FORMAT))
        // And control the format
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        om.enable(SerializationFeature.INDENT_OUTPUT) // todo only on prod
        // this allows us to ignore properties from the UI that we don't know
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        /*  fromJson() Serialization Features */
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // if false, default value is used (0 for 'int', 0.0 for double,
        om.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

        LoggerFactory.getLogger(JacksonConfig::class.java)
            .debug("Primary Jackson Object Mapper successfully configured: $om")
        return om
    }

}
