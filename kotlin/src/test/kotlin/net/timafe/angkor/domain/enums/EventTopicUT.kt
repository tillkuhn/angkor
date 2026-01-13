package net.timafe.angkor.domain.enums

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventTopicUT {

    @Test
    fun `it should prefix topics`() {
        assertEquals("prefix.app.events", EventTopic.APP.withPrefix("prefix."))
    }


    @Test
    fun `it should return a list that contains app events topic`() {
        assertTrue(EventTopic.allTopics().toString().contains("app.events"))
    }

}
