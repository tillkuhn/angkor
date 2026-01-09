package net.timafe.angkor.service.utils

import net.timafe.angkor.domain.interfaces.Taggable
import net.timafe.angkor.service.utils.TaggingUtils.Companion.normalizeTag
import org.slf4j.LoggerFactory

class TaggingUtils {

    companion object {

        /**
         * mergeAndSort Takes the given list of tags and adds to the list
         * of the item's existing Tags, unless the tag already exists
         *
         * Also ensures the resulting list only contains distinct values
         * and is sorted in ascending order (implicitly uses [normalizeTag])
         *
         * This function returns nothing. it mutates the input list!
         */
        fun mergeAndSort(item: Taggable, tagsToAdd: List<String>) {
            val log = LoggerFactory.getLogger(TaggingUtils::class.java)
            for (tag in tagsToAdd) {
                val normalizedTags = normalizeTag(tag)
                if (!item.tags.contains(normalizedTags)) {
                    log.trace("Adding tag $normalizedTags to $item")
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

}
