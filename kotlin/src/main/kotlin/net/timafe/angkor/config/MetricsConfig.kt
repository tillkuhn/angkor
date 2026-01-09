package net.timafe.angkor.config

import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.config.MeterFilterReply
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MetricsConfig {

    companion object {
        val FilterNames = setOf(
            "hikaricp.connections.max",
            "hikaricp.connections.active",
            "hikaricp.connections.acquire",
            "hikaricp.connections",
            "jvm.memory.max",
            "jvm.memory.committed",
            "jvm.memory.used",
            "process.start.time",
            "process.uptime",
            "system.cpu.usage",
            "tomcat.sessions.active.current",
            "tomcat.sessions.active.max",
            "tomcat.sessions.created"
        )
    }

    // add common tags
    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            registry.config().commonTags("app", "angkor-api")
        }
    }

    /**
     * Reduce metrics https://stackoverflow.com/a/54097060/4292075
     * https://github.com/micrometer-metrics/micrometer-docs/blob/main/src/docs/concepts/meter-filters.adoc#deny-or-accept-meters
     * Example Names (it uses dots, not the prometheus style underscore notation)
     *   http.server.requests.active
     *   spring.security.filter.active     *
     */

    @Bean
    fun meterFilter(): MeterFilter {
        return object : MeterFilter {
            override fun accept(id: Meter.Id): MeterFilterReply {
                //listOf("jdbc.connections.active", "jvm.memory.max","process.cpu.usage","process.start").forEach {
                FilterNames.forEach {
                    if (id.name.startsWith(it)) {
                        return MeterFilterReply.NEUTRAL
                    }
                }
                // println(id.name)
//                if (id.getName().startsWith("jvm.")) {
//                    return MeterFilterReply.DENY
//                }
                return MeterFilterReply.DENY // deny all other
            }
        }
    }

}
