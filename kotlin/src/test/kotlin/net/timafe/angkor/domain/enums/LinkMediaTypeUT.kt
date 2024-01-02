package net.timafe.angkor.domain.enums

import net.timafe.angkor.service.LinkService
import net.timafe.angkor.web.LinkController
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class LinkMediaTypeUT {

    @Test
    fun testLinkMediaTypes() {
        val types = LinkController(Mockito.mock(LinkService::class.java)).getLinkMediaTypes()
        Assertions.assertThat(types.size).isEqualTo(Media_Type.values().size)
        types.forEach{ Assertions.assertThat(it.icon).isNotBlank()  }
        types.forEach{ Assertions.assertThat(it.label).isNotBlank()  }
    }

}
