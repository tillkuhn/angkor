package net.timafe.angkor.config

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import tools.jackson.databind.json.JsonMapper
import kotlin.collections.contains


/**
 * Configure Jackson JSON Serialization
 */
@Configuration
class JacksonConfig {


    // Jackson 3 Feature Customizer, apparently ObjectMapper below no longer kicks in for
    // Deserialization, so for instance SearchRequests without page param get rejected, see also  https://stackoverflow.com/a/79854456/4292075
    // Guide 1: https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring
    // Guide 2: https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md
    @Bean
    fun customizer(env: Environment): JsonMapperBuilderCustomizer {
        LoggerFactory.getLogger(JacksonConfig::class.java)
            .debug("[Config] Configuring Jackson3 JsonMapperBuilderCustomizer with Kotlin Support")
        return JsonMapperBuilderCustomizer { builder: JsonMapper.Builder? ->
            builder!!
                .changeDefaultPropertyInclusion { incl: JsonInclude.Value? ->
                    incl!!.withValueInclusion(
                        JsonInclude.Include.NON_NULL
                    )
                }
                .changeDefaultPropertyInclusion { incl: JsonInclude.Value? ->
                    incl!!.withContentInclusion(
                        JsonInclude.Include.NON_NULL
                    )
                }
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(tools.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .apply {
                    // not sure whether indent output true is default or not, so let's make it explicit
                    if (env.activeProfiles.contains("prod")) {
                        // disable pretty print in prod, optimize for performance / bandwidth
                        disable(SerializationFeature.INDENT_OUTPUT)
                    } else {
                        // enable pretty print in non-prod envs
                        enable(SerializationFeature.INDENT_OUTPUT)
                    }
                }
                // KolinModule: FIX Jackson 3.x issues with (...) Parameter specified as non-null is null:
                // https://github.com/FasterXML/jackson-module-kotlin
                .addModule(tools.jackson.module.kotlin.KotlinModule.Builder().build())
        }
    }
    /**
     * CAUTION: We must make sure EnableWebMvc does also use *this* mapper, see [WebConfig] Configuration
     * - https://www.baeldung.com/spring-boot-customize-jackson-objectmapper
     * - https://stackoverflow.com/questions/55811031/is-it-possible-to-simplify-jsonserialize-annotations
     * - https://codingnconcepts.com/spring-boot/jackson-json-request-response-mapping
     */
    @Deprecated("Use JSONMapper instead")
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val om = ObjectMapper()

        /* Modules */

        // The @JsonFormat annotation on properties for local dates is now obsolete, hopefully
        // See https://stackoverflow.com/a/60547263/4292075
        om.registerModule(JavaTimeModule())
        om.registerModule(Jdk8Module())
        // we need this, or we get null values during serialization for props not present in JSON
        // https://github.com/FasterXML/jackson-module-kotlin/issues/177
        // https://github.com/FasterXML/jackson-module-kotlin/issues/130
        om.registerModule(KotlinModule.Builder().build())

        /*  toJson()  Serialization Features */
        // important to get 2020-07-21T14:33:31.407Z format
        // To get even more control, maybe we can add serializer
        // LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.JACKSON_DATE_TIME_FORMAT))
        // And control the format
        om.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        om.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT) // todo only on prod
        // this allows us to ignore properties from the UI that we don't know
        //
        om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL) // instead of om.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        /*  fromJson() Serialization Features */
        om.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // if false, default value is used (0 for 'int', 0.0 for double,
        om.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

        LoggerFactory.getLogger(JacksonConfig::class.java)
            .debug("[Config] Primary Jackson Object Mapper successfully configured: {}", om)
        return om
    }

}
