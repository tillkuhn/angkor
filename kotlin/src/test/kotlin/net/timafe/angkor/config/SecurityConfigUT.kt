package net.timafe.angkor.config

import net.timafe.angkor.domain.enums.EntityType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class SecurityConfigUT {

    @Test
    fun `it should create entity patterns for each entity type`() {
        val sc = SecurityConfig()
        val patterns = sc.getEntityPatterns("/hase")
        Assertions.assertThat(patterns.size).isEqualTo(EntityType.values().size)
    }
}
