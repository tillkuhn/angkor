package net.timafe.angkor.domain.dto

import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.domain.enums.LocationType
import org.springframework.beans.factory.annotation.Value
import java.util.*

interface TagSummary {

    var label: String
    var count: Int

}
