package net.timafe.angkor.domain.enums

import net.timafe.angkor.domain.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertContains
import kotlin.test.assertEquals

class EntityTypeUT {

    @Test
    fun `it should derive entity type from class annotation`() {
        val note = Mockito.mock(Note::class.java)
        assertEquals(EntityType.NOTE,EntityType.fromEntityAnnotation(note))
    }

    @Test
    fun `it should convert form class to enum`() {
        assertEquals(EntityType.PLACE,EntityType.fromEntityClass(Place::class.java))
        assertEquals(EntityType.PLACE,EntityType.fromEntityClass(PlaceV2::class.java))
    }

    @Test
    fun `whenExceptionThrown thenExpectationSatisfied`() {
        val ex = Assertions.assertThrows(IllegalArgumentException::class.java) {
            EntityType.fromEntityClass(AbstractBaseEntity::class.java)
        }
        assertContains(ex.message!!, "cannot derive any entityType from class")
    }
}
