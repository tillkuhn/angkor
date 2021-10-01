package net.timafe.angkor.repo

import net.timafe.angkor.domain.Location
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LocationRepository: CrudRepository<Location, UUID>
