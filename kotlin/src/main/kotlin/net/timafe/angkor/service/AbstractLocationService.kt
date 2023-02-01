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
            log.debug("AreaCode ${item.areaCode} or GeoAddress ${item.geoAddress} empty, lookup geoPoint for ${item.coordinates}")
            try {
                // Call geo service, attempt to lookup country code and geoAddress
                val pInfo = geoService.reverseLookupWithRateLimit(Coordinates(item.coordinates))
                log.debug("[Location] Lookup geoPoint for ${item.coordinates} result: $pInfo")
                // pInfo country may also be null (see commends in GeoService
                // So we only "correct" our current area code if the geo country code is different and our
                // current area code dos not start with that country (e.g. area code is th-gulf but geoCountry is vn)
                if (pInfo.countryCode != null && areaCodeUpdateRequired(pInfo.countryCode,item.areaCode) ) {
                    log.debug("[Location] Replacing previous areaCode ${item.areaCode} with geoPoint country ${pInfo.countryCode}")
                    item.areaCode = pInfo.countryCode
                } else {
                    log.debug("[Location] Keeping ${item.areaCode} instead of geoPoint country ${pInfo.countryCode}")
                }
                item.geoAddress = pInfo.name
            } catch (rateLimitExceeded: GeoService.RateLimitException) {
                log.warn("[Location] Could not query Service due to Rate Limit, try again later: ${rateLimitExceeded.message}")
            }
        }
        return super.save(item)
    }

    private fun areaCodeUpdateRequired(newCode: String, oldAreaCode: String?): Boolean {
        return when {
            oldAreaCode == null -> true
            oldAreaCode.startsWith(newCode) -> false
            else -> true
        }
    }

}
