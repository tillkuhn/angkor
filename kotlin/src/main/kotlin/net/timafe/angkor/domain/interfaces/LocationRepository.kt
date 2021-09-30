package net.timafe.angkor.domain.interfaces

import net.timafe.angkor.domain.Location
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LocationRepository: CrudRepository<Location, UUID>
