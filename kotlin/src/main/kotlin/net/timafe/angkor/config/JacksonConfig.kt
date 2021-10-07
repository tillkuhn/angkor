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

    // Make sure EnableWebMvc does also use our mapper, see WebConfig Configuration
    // https://www.baeldung.com/spring-boot-customize-jackson-objectmapper
    // https://stackoverflow.com/questions/55811031/is-it-possible-to-simplify-jsonserialize-annotations
    // https://codingnconcepts.com/spring-boot/jackson-json-request-response-mapping
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val om = ObjectMapper()
        // This @JsonFormat annotation on properties for local dates obsolete, hopefully
        // See https://stackoverflow.com/a/60547263/4292075
        om.registerModule(JavaTimeModule())
        om.registerModule(Jdk8Module())
        // wee need this, or we get null values during serialization for props not present in json
        // https://github.com/FasterXML/jackson-module-kotlin/issues/177
        om.registerModule(KotlinModule())
        // important to get 2020-07-21T14:33:31.407Z format
        // To get even more control, maybe we can add serializer
        // LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.JACKSON_DATE_TIME_FORMAT))
        // And control the format
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // this allows us to ignore properties from the UI that we don't know
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        om.enable(SerializationFeature.INDENT_OUTPUT) // todo only on prod
        LoggerFactory.getLogger(JacksonConfig::class.java).info("Jackson is here with a custom $om")
        return om
    }

//    @Bean
//    @Primary
//    open fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder? {
//        LoggerFactory.getLogger(JacksonConfig::class.java).info("Jackson is here")
//            return Jackson2ObjectMapperBuilder().serializers(
//                LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.JACKSON_DATE_TIME_FORMAT)))
//            .serializationInclusion(JsonInclude.Include.NON_NULL)
//                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .modules(JavaTimeModule(),Jdk8Module() )
//    }
//    fun customJson(): Jackson2ObjectMapperBuilderCustomizer? {
//        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
//            builder.indentOutput(true)
//        }
//    }

}
