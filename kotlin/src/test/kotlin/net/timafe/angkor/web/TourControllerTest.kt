package net.timafe.angkor.web

import net.timafe.angkor.domain.Tour
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class TourControllerTest(private val controller: TourController) {

    fun testCRUD() {
        val initialCount = controller.findAll().size
        val tour = Tour(tourUrl = "https://some.tour/1234")
        tour.apply {
            name = "testTour"
        }
        val created = controller.create(tour)
        val id = created.id
        assertEquals(initialCount+1,controller.findAll().size)

        val delTour = controller.delete(id)
        assertEquals(HttpStatus.OK,delTour.statusCode)
        assertEquals(initialCount,controller.findAll().size)
    }

}
