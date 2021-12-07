package net.timafe.angkor.domain.enums

import net.timafe.angkor.domain.AbstractBaseEntity
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.dto.MetricDetails
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityTypeUT {

    @Test
    fun `it should convert form class to enum`() {
        assertEquals(EntityType.Place,EntityType.fromEntityClass(Place::class.java))
        assertEquals(EntityType.Place,EntityType.fromEntityClass(Place::class.java))
    }

    @Test
    fun `whenExceptionThrown thenExpectationSatisfied`() {
        val ex = Assertions.assertThrows(IllegalArgumentException::class.java) {
            EntityType.fromEntityClass(AbstractBaseEntity::class.java)
        }
        assertContains(ex.message!!, "cannot derive any entityType from class")
    }

    @Test
    fun `it should derive entity type from plural path`() {
        assertEquals(EntityType.Dish, EntityType.fromEntityPath("dishes"))
    }

    @Test
    fun `testManagedEntity from ManagedEntity Annotation`() {
        val et = EntityType.fromEntityClass(Dish::class.java)
        org.assertj.core.api.Assertions.assertThat(et).isEqualTo(EntityType.Dish)

        val noManagedEntityAnnotation = MetricDetails(name="pets",baseUnit = "cats",description = "",value = "3")
        assertFailsWith<IllegalArgumentException> {
            EntityType.fromEntityClass(noManagedEntityAnnotation.javaClass)
        }
    }

    @Test
    fun `it should lookup type from path`() {
        assertEquals(EntityType.fromEntityPath("posts"),EntityType.Post)
        assertEquals(EntityType.fromEntityPath("/dishes"),EntityType.Dish)
        assertEquals(EntityType.fromEntityPath("tours/"),EntityType.Tour)
        assertFailsWith<IllegalArgumentException> {
            EntityType.fromEntityPath("no.such.path")
        }
    }
}
