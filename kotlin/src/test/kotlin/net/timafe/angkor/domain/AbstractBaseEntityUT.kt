package net.timafe.angkor.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AbstractBaseEntityUT {

    @Test
    fun `it should ensure that objects with different id do not qualify as equal`() {
        val place1 = Place(notes = "")
        val place2 = Place(notes = "")
        assertNotEquals(place1,place2)
    }

    @Test
    fun `it should ensure that objects with same id do qualify as equal`() {
        val place1 = Place(notes = "")
        val place2 = Place(notes = "")
        place2.id = place1.id
        assertEquals(place1,place2)
        assertEquals(place1.hashCode(),place2.hashCode(),"Should share the same id hashcode")
        assertContains(place1.toString(), place1.id.toString())
    }

}
