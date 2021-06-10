package net.timafe.angkor.incubator

import net.timafe.angkor.config.annotations.ManagedEntity
import net.timafe.angkor.domain.dto.MetricDTO
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.helper.TestHelpers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull


class TestAnnotations {

    @Test
    fun testManagedEntity() {
        val place = TestHelpers.somePlace()
        val et = EntityType.fromEntityAnnotation(place)
        assertThat(et).isEqualTo(EntityType.PLACE)

        val noManagedEntityAnnotation = MetricDTO(name="pets",baseUnit = "cats",description = "",value = "3")
        assertFailsWith<IllegalArgumentException> {
            EntityType.fromEntityAnnotation(noManagedEntityAnnotation)
        }
        //val idProp = obj::class.declaredMemberProperties.first { it.name == "id" }

        //        println("# property: " + idProp) // "val ...TestData.id: kotlin.Int"
        //        println("# annotations: " + idProp.annotations) // [] - but why?
        //        println("# findAnnotations<Foo>: " + idProp.findAnnotation<Foo>()) // null - but why?
        //
        //        assertTrue(idProp.name.endsWith("TestData.id: kotlin.Int"))
        //        assertEquals(1, idProp.annotations.size, "Bug! No annotations found!")
        //        assertNotNull(idProp.findAnnotation<Foo>())
    }

}
