package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.GeoPoint
import net.timafe.angkor.helper.TestHelpers
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.kafka.KafkaProperties

class MockServices {

    companion object {
        fun geoService(): GeoService {
            val geoService = Mockito.mock(GeoService::class.java)
            val geoPoint = GeoPoint(123, 1.0, 2.0, "th", "temple", "Thailand")
            Mockito.`when`(geoService.reverseLookup(TestHelpers.mockitoAny())).thenReturn(geoPoint)
            Mockito.`when`(geoService.reverseLookupWithRateLimit(TestHelpers.mockitoAny())).thenReturn(geoPoint)
            return geoService
        }


        fun kafkaProperties(): KafkaProperties {
            val props = Mockito.mock(KafkaProperties::class.java)
            val sec = Mockito.mock(KafkaProperties.Security::class.java)
            val kProps = mutableMapOf<String,String>()
            kProps["sasl.mechanism"] = "SCRAM-SHA-256"
            kProps["sasl.jaas.config"] = "Da hab ich den Jazz erfunden"
            Mockito.`when`(sec.protocol).thenReturn("SASL_SSL")
            Mockito.`when`(props.security).thenReturn(sec)
            Mockito.`when`(props.properties).thenReturn(kProps)
            Mockito.`when`(props.bootstrapServers).thenReturn(listOf("kafka.nock.io"))
            return props
        }
    }
}
