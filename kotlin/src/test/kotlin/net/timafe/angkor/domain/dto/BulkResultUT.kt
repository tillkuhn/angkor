package net.timafe.angkor.domain.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

// When using this mode, a new test instance will be created once per test class.
// https://phauer.com/2018/best-practices-unit-testing-kotlin/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BulkResultUT {

    @Test
    fun testAdd() {
        val br = BulkResult( read = 2, inserted = 1)
        val br2 = BulkResult( read = 7, updated = 2, inserted = 2)
        br.add(br2)
        assertEquals(9,br.read)
        assertEquals(3,br.inserted)
        assertEquals(2,br.updated)
    }
}
