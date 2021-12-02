package net.timafe.angkor.service.interfaces

import net.timafe.angkor.domain.dto.BulkResult

interface Importer {

    fun importAsync()
    fun import(): BulkResult

}
