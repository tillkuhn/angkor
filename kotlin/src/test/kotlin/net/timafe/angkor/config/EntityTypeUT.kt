package net.timafe.angkor.config

import net.timafe.angkor.domain.dto.MetricDTO
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.helper.TestHelpers
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityTypeUT {

    @Test
    fun `entity type title case`() {
        assertEquals(EntityType.POST.titlecase(),"Post")
    }

    @Test
    fun `testManagedEntity Annotations`() {
        val place = TestHelpers.somePlace()
        val et = EntityType.fromEntityAnnotation(place)
        Assertions.assertThat(et).isEqualTo(EntityType.PLACE)

        val noManagedEntityAnnotation = MetricDTO(name="pets",baseUnit = "cats",description = "",value = "3")
        assertFailsWith<IllegalArgumentException> {
            EntityType.fromEntityAnnotation(noManagedEntityAnnotation)
        }

    }

}
