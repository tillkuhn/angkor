package net.timafe.angkor.service

import net.timafe.angkor.domain.interfaces.Taggable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TaggingService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * mergeAndSort Takes the given list of tags and adds to the list
     * of the item's existing Tags if the tag does not exist yet
     *
     * Also ensures the resulting list only contains distinct values
     * and is sorted in ascending order
     */
    fun mergeAndSort(item: Taggable, tags: List<String>) {
        for (tag in tags) {
            val normalizedTags = normalizeTag(tag)
            if (!item.tags.contains(normalizedTags)) {
                log.debug("Adding tag $normalizedTags to $item")
                item.tags.add(normalizedTags)
            }
        }
        item.tags = item.tags.distinct().toMutableList()
        item.tags.sort()
    }

    fun normalizeTag(tag: String): String {
        return tag.trim().replace(" ", "-").lowercase()
    }
}
