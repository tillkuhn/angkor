package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Note
import net.timafe.angkor.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(Constants.API_DEFAULT_VERSION + "/metrics")
/**
 * https://stackoverflow.com/questions/32382349/how-to-get-metrics-from-spring-boot-actuator-programmatically
 */
class MetricsController(private val metrics: MetricsEndpoint) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun metrics(): List<MetricsEndpoint.MetricResponse> {
        val m = mutableListOf<MetricsEndpoint.MetricResponse>()
        metrics.listNames().names.forEach{
            log.info("Retrieving $it");
            m.add(metrics.metric(it,null));
        }
        // val dishes = if (principal != null)  placeRepository.findByOrderByName() else placeRepository.findPublicPlaces()
        //  coo ${places.get(0).coordinates}"
        //log.info("allNotes() return ${entities.size} notes principal=${principal}")
        return m
    }


}
