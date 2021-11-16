package net.timafe.angkor.domain.enums

import net.timafe.angkor.domain.*
import net.timafe.angkor.domain.dto.MetricDetails
import net.timafe.angkor.helper.TestHelpers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `entity type title case`() {
        assertEquals(EntityType.POST.titlecase(),"Post")
    }

    @Test
    fun `testManagedEntity Annotations`() {
        val place = TestHelpers.somePlace()
        val et = EntityType.fromEntityAnnotation(place)
        org.assertj.core.api.Assertions.assertThat(et).isEqualTo(EntityType.PLACE)

        val noManagedEntityAnnotation = MetricDetails(name="pets",baseUnit = "cats",description = "",value = "3")
        assertFailsWith<IllegalArgumentException> {
            EntityType.fromEntityAnnotation(noManagedEntityAnnotation)
        }

    }
}
