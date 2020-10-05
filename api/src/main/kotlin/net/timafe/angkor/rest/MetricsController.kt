package net.timafe.angkor.rest

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.MetricDTO
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.core.SpringVersion
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
// @RequestMapping(Constants.API_DEFAULT_VERSION + "/admin")
/**
 * https://stackoverflow.com/questions/32382349/how-to-get-metrics-from-spring-boot-actuator-programmatically
 */
class MetricsController(private val metrics: MetricsEndpoint, private val objectMapper: ObjectMapper) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        val filterNames = setOf("hikaricp.connections.max",
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
                "tomcat.sessions.created")
    }

    // @PreAuthorize(Constants.ADMIN_AUTHORITY)
    @GetMapping("${Constants.API_DEFAULT_VERSION}/admin/metrics")
    @ResponseStatus(HttpStatus.OK)
    fun metrics(): List<MetricDTO> {
        val meli = mutableListOf<MetricDTO>()
        meli.add(MetricDTO("spring.version", "Spring Framework Version",SpringVersion.getVersion(),null))
        meli.add(MetricDTO("java.version", "Java Major Minor Version",System.getProperty("java.version"),null))
        meli.add(MetricDTO("kotlin.version", "Kotlin Version",KotlinVersion.CURRENT.toString(),null))
        metrics.listNames().names.filter { filterNames.contains(it) }
                .forEach {
                    // example it: tomcat.sessions.active.current
                    val resp: MetricsEndpoint.MetricResponse = metrics.metric(it, null)
                    // log.trace(objectMapper.writeValueAsString(resp))
                    val metricDTO = MetricDTO(resp.name, resp.description, resp.measurements.get(0).value.toString(), resp.baseUnit)
                    meli.add(metricDTO)
                }
        return meli
    }

/*    @GetMapping("${Constants.API_DEFAULT_VERSION}/metrics")
    @ResponseStatus(HttpStatus.OK)
    fun publicMetrics(): List<MetricDTO> {
        return this.metrics()
    }*/

}
