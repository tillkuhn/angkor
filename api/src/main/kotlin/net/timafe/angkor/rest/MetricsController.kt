package net.timafe.angkor.rest

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.dto.MetricDTO
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/admin")
/**
 * https://stackoverflow.com/questions/32382349/how-to-get-metrics-from-spring-boot-actuator-programmatically
 */
class MetricsController(private val metrics: MetricsEndpoint, private val objectMapper: ObjectMapper) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/metrics")
    @ResponseStatus(HttpStatus.OK)
    fun metrics(): List<MetricDTO> {
        val meli = mutableListOf<MetricDTO>()
        metrics.listNames().names.forEach{
            // example it: tomcat.sessions.active.current
            val resp: MetricsEndpoint.MetricResponse = metrics.metric(it,null)
            log.trace(objectMapper.writeValueAsString(resp))
            val metricDTO = MetricDTO(resp.name,resp.description,resp.measurements.get(0).value,resp.baseUnit)
            meli.add(metricDTO)
        }
        return meli
    }


}
