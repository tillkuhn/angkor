package net.timafe.angkor.domain.dto

import net.timafe.angkor.domain.enums.EntityType

/**
 * Helper Data Class to encapsulate properties for Importing entities from external Sources
 */
data class ImportRequest(
    val importUrl: String,
    val targetEntityType: EntityType,
)
