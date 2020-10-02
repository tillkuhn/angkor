package net.timafe.angkor

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.repo.DishRepository
import net.timafe.angkor.service.AuthService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase

@ActiveProfiles(Constants.PROFILE_TEST, Constants.PROFILE_CLEAN)
@DataJpaTest
// required so we take the existing db as configured in app properties for test profile
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
class JPATests {

    @Autowired
    lateinit var dishRepository: DishRepository

    @Test
    fun testAll() {
        assertThat(dishRepository.findAll().size).isGreaterThan(1)
    }

    @Test
    fun testNativeSQL() {
        val scopes = AuthService.authScopesAsString(listOf(AuthScope.PUBLIC))
        assertThat(dishRepository.search("",scopes).size).isGreaterThan(0)
    }
}
