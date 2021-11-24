package net.timafe.angkor.service

import net.timafe.angkor.domain.LocatableEntity
import net.timafe.angkor.domain.dto.Coordinates
import org.springframework.data.repository.CrudRepository

/**
 * Superclass for standard entity services
 */
abstract class AbstractLocationService<ET: LocatableEntity, EST, ID> (
    repo: CrudRepository<ET, ID>,
    protected val geoService: GeoService,
): AbstractEntityService<ET, EST, ID>(repo)  {

    /**
     * LocationSave eventually delegates to repo.save(item)
     * vzt ensures that we look up countryCode and geoAddress based on coordinates if either is empty
     */
    override fun save(item: ET): ET {
        if (item.hasCoordinates() && (item.areaCode == null || item.geoAddress == null)) {
            log.debug("AreaCode or GeoAddress empty, lookup country for ${item.coordinates}")
            try {
                // Call geo service, attempt to lookup country code and geoAddress
                val pInfo = geoService.reverseLookupWithRateLimit(Coordinates(item.coordinates))
                log.debug("Lookup country for ${item.coordinates} result: $pInfo")
                item.areaCode = pInfo.countryCode
                item.geoAddress = pInfo.name
            } catch (rateLimitExceeded: GeoService.RateLimitException) {
                log.warn("Could not query Service due to Rate Limit, try again later: ${rateLimitExceeded.message}")
            }
        }
        return super.save(item)
    }

}
