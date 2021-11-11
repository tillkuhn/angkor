package net.timafe.angkor.service

import net.timafe.angkor.domain.Location
import net.timafe.angkor.domain.dto.Coordinates
import org.springframework.data.repository.CrudRepository

/**
 * Superclass for standard entity services
 */
abstract class AbstractLocationService<ET: Location, EST, ID> (
    repo: CrudRepository<ET, ID>,
    private val geoService: GeoService,
): AbstractEntityService<ET, EST, ID>(repo)  {

    /**
     * LocationSave ensures that we look up countryCode and geoAddress based on coordinates
     */
    override fun save(item: ET): ET {
        if (item.hasCoordinates() && (item.areaCode == null || item.geoAddress == null)) {
            // Call geo service, attempt to lookup country
            log.debug("Lookup country for ${item.coordinates}")
            val pInfo = geoService.reverseLookup(Coordinates(item.coordinates))
            log.debug("Lookup country for ${item.coordinates} result: $pInfo")
            item.areaCode = pInfo?.countryCode
            item.geoAddress = pInfo?.name
        }
        return super.save(item)
    }

}
