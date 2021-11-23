package net.timafe.angkor.service

import net.timafe.angkor.service.utils.TaggingUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TaggingServiceUT {

    // https://www.baeldung.com/parameterized-tests-junit-5#4-csv-literals
    @ParameterizedTest
    @CsvSource(value= ["Sri Lanka North:sri-lanka-north", "Hase:hase"], delimiter = ':')
    fun testTags(code: String, expected: String) {
        Assertions.assertThat(TaggingUtils.normalizeTag(code)).isEqualTo(expected)
    }

}
