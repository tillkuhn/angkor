package net.timafe.angkor.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TaggingServiceUT {

    // https://www.baeldung.com/parameterized-tests-junit-5#4-csv-literals
    @ParameterizedTest
    @CsvSource(value= ["Sri Lanka North:sri-lanka-north", "Hase:hase"], delimiter = ':')
    fun testTags(code: String, expected: String) {
        val taggingService = TaggingService()
        Assertions.assertThat(taggingService.normalizeTag(code)).isEqualTo(expected)
    }

}
