package net.timafe.angkor.web

import net.timafe.angkor.domain.dto.SearchRequest
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import org.assertj.core.api.Assertions
import org.springframework.data.domain.Sort
import kotlin.test.assertNotNull

class LocationSearchControllerTest(private val locationController: LocationSearchController) {

    fun testPublic() {
        val locations = locationController.searchAll()
        val tours =
            locationController.search(SearchRequest(entityTypes = mutableListOf(EntityType.Tour)))
        val testSearch = SearchRequest(entityTypes = mutableListOf(EntityType.Video), query = "test")
        // no coverage for asPageable() anywhere else,so let's test it here
        assertNotNull(testSearch.asPageable())

        val videos =
            locationController.search(testSearch)
        val toursAndVideos = locationController.search(
            SearchRequest(
                entityTypes = mutableListOf(EntityType.Video, EntityType.Tour),
                query = "test", sortDirection = Sort.Direction.DESC, sortProperties = mutableListOf("updatedAt", "name")
            )
        )
        Assertions.assertThat(locations.size).isGreaterThanOrEqualTo(tours.size + videos.size)
        Assertions.assertThat(tours.size).isGreaterThan(0)
        tours.iterator().forEach {
            Assertions.assertThat(it.updatedAt!!.year).isGreaterThan(2000)
            assertNotNull(it.updatedBy)
        }
        Assertions.assertThat(videos.size).isGreaterThan(0)
        Assertions.assertThat(toursAndVideos.size).isEqualTo(tours.size + videos.size)
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            // notes are not a supported entity type here, so this should throw IAE
            locationController.search(SearchRequest(entityTypes = mutableListOf(EntityType.Note)))
        }
    }

    fun testRestricted() {
        val tours = locationController.search(SearchRequest(entityTypes = mutableListOf(EntityType.Tour)))
        for (tour in tours) {
            if (tour.authScope == AuthScope.RESTRICTED) {
                return // no need to continue, one restricted tour is proof enough
            }
        }
        throw IllegalStateException("Expected at least one restricted tour")
    }
}
