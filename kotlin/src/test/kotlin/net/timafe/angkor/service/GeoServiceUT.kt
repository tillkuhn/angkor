package net.timafe.angkor.service

import net.timafe.angkor.domain.dto.Coordinates
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeoServiceUT {

    private val geoService = GeoService()

    @Test
    fun `it should locate Bangkok In Thailand`() {
        val coordinates = Coordinates(100.4898632,13.7435571)
        val resp = geoService.reverseLookup(coordinates)
        /*
         {
            "place_id": 106523360,
            "licence": "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright",
            "osm_type": "way",
            "osm_id": 23481741,
            "lat": "13.743913150000001",
            "lon": "100.48848833622938",
            "place_rank": 30,
            "category": "amenity",
            "type": "place_of_worship",
            "importance": 0.3817760669396329,
            "addresstype": "amenity",
            "name": "วัดอรุณราชวรารามราชวรมหาวิหาร",
            "display_name": "วัดอรุณราชวรารามราชวรมหาวิหาร, 158, Thanon Wang Doem, แขวงวัดอรุณ, เขตบางกอกใหญ่, กรุงเทพมหานคร, 10600, ประเทศไทย",
            "address": {
                "amenity": "วัดอรุณราชวรารามราชวรมหาวิหาร",
                "house_number": "158",
                "road": "Thanon Wang Doem",
                "quarter": "แขวงวัดอรุณ",
                "suburb": "เขตบางกอกใหญ่",
                "city": "กรุงเทพมหานคร",
                "state": "กรุงเทพมหานคร",
                "postcode": "10600",
                "country": "ประเทศไทย",
                "country_code": "th"
             },
            "boundingbox": [
                "13.7427134",
                "13.7450935",
                "100.4869145",
                "100.4898908"
            ]
        }
        */
        assertNotNull(resp)
        assertEquals(23481741,resp.osmId)
        assertEquals(106523360,resp.placeId)
        assertEquals("th",resp.countryCode)
        assertTrue(resp.name!!.isNotEmpty())
        assertTrue(resp.type!!.isNotEmpty())
        Assertions.assertThat(resp.lat).isGreaterThan(0.0)
        Assertions.assertThat(resp.lon).isGreaterThan(0.0)
    }
}
