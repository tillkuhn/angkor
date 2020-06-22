package net.timafe.angkor

import net.timafe.angkor.config.Constants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.PROFILE_TEST)
class IntegrationTests(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `Assert greeting content and status code`() {
        val entity = restTemplate.getForEntity<String>("/greeting",String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains("World")
    }

}
