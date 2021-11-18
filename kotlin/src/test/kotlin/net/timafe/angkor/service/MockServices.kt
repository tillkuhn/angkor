package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.GeoPoint
import net.timafe.angkor.helper.TestHelpers
import org.mockito.Mockito

class MockServices {

    companion object {
        fun geoService(): GeoService {
            val geoService = Mockito.mock(GeoService::class.java)
            val geoPoint = GeoPoint(123, 1.0, 2.0, "th", "temple", "Thailand")
            Mockito.`when`(geoService.reverseLookup(TestHelpers.mockitoAny())).thenReturn(geoPoint)
            Mockito.`when`(geoService.reverseLookupWithRateLimit(TestHelpers.mockitoAny())).thenReturn(geoPoint)
            return geoService
        }

        fun locationSearch(): LocationSearchService {
            return Mockito.mock(LocationSearchService::class.java)
        }
    }
}
