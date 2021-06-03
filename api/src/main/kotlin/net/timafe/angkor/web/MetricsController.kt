package net.timafe.angkor.web

import com.fasterxml.jackson.databind.JsonNode
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.MetricDTO
import net.timafe.angkor.service.MetricsService
import net.timafe.angkor.web.vm.BooleanResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootVersion
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.core.SpringVersion
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * https://stackoverflow.com/questions/32382349/how-to-get-metrics-from-spring-boot-actuator-programmatically
 */
@RestController
class MetricsController(
    private val metricsEndpoint: MetricsEndpoint,
    private val appProperties: AppProperties,
    private val stats: MetricsService
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        val filterNames = setOf(
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

    @GetMapping("${Constants.API_LATEST}/stats")
    fun entityStats(): Map<String, Long> {
        return stats.entityStats()
    }

    // @PreAuthorize(Constants.ADMIN_AUTHORITY)
    @GetMapping("${Constants.API_LATEST}/admin/metrics")
    @ResponseStatus(HttpStatus.OK)
    fun metrics(): List<MetricDTO> {
        val metrics = mutableListOf<MetricDTO>()
        metrics.add(MetricDTO("spring-boot.version", "Spring Boot Version", SpringBootVersion.getVersion(), null))
        metrics.add(MetricDTO("spring.version", "Spring Framework Version", SpringVersion.getVersion(), null))
        metrics.add(MetricDTO("java.version", "Java Major Minor Version", System.getProperty("java.version"), null))
        metrics.add(MetricDTO("kotlin.version", "Kotlin Version", KotlinVersion.CURRENT.toString(), null))
        metrics.add(MetricDTO("app.version", "App Version (API)", appProperties.version, null))
        metrics.addAll(metricsEndpoint.listNames().names
            .filter { filterNames.contains(it) }
            .map {
                val resp: MetricsEndpoint.MetricResponse = metricsEndpoint.metric(it, null)
                MetricDTO(resp.name, resp.description, resp.measurements[0].value, resp.baseUnit)
            }
        )
        return metrics
    }

    @GetMapping("${Constants.API_LATEST}/metrics")
    @ResponseStatus(HttpStatus.OK)
    fun publicMetrics(): List<MetricDTO> {
        return this.metrics()
    }

    /**
     * Evaluate github hooks
     *
     * https://docs.github.com/en/developers/webhooks-and-events/webhooks/creating-webhooks
     */
    @PostMapping("/webhooks/github")
    @ResponseStatus(HttpStatus.OK)
    fun githubWebhooks(
        @RequestBody requestBody: JsonNode,
        @RequestHeader headers: HttpHeaders
    ): BooleanResult {
        //
        val sig = headers.get("X-Hub-Signature-256")
        log.info("[webhooks] Received github event with sig $sig: $requestBody")
        return BooleanResult(true)
    }

}
