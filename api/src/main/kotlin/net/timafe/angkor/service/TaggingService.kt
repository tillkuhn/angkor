package net.timafe.angkor.service

import net.timafe.angkor.domain.interfaces.Taggable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TaggingService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun mergeAndSort(item: Taggable, tags: List<String>) {
        for (tag in tags) {
            val normalizedTags = normalizeTag(tag)
            if (!item.tags.contains(normalizedTags)) {
                log.debug("Adding tag $normalizedTags to $item")
                item.tags.add(normalizedTags)
            }
        }
        item.tags.sort()
    }

    fun normalizeTag(tag: String): String {
        return tag.trim().replace(" ", "-").toLowerCase()
    }
}
