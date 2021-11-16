package net.timafe.angkor.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

/**
 * Service Implementation for evicting Caches (more to come ...)
 */
@Service
class CacheService(
    private val cacheManager: CacheManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Clear cache by name
     *
     * https://www.baeldung.com/spring-boot-evict-cache#2-using-cachemanager
     * https://www.baeldung.com/spring-cache-tutorial#2-cacheevict
     * Alternative: Use @CacheEvict( BLA_CACHE, allEntries = true) on method
     */
    fun clearCache(vararg cacheNames: String) {
        for (cacheName in cacheNames) {
            val cache = cacheManager.getCache(cacheName)
            if (cache != null) {
                this.log.debug("Clearing cache cacheName=$cache")
                cache.clear() // clears all. evict would be more fine-grained, but we're not there yet
                // cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)?.evict(user.email!!)
            } else {
                log.warn("cacheName=$cacheName does not exist (yet?), skip clear()")
            }
        }
    }

}
