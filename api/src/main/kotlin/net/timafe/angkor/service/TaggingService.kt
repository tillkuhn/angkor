package net.timafe.angkor.service

import net.timafe.angkor.domain.interfaces.Taggable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TaggingService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun mergeTags(item: Taggable, vararg tags: String) {
        for (tag in tags) {
            val nota = normalizeTag(tag)
            if (!item.tags.contains(nota)) {
                log.debug("Adding tag $nota to $item")
                item.tags.add(nota)
            }
        }
        item.tags.sort()
    }

    fun normalizeTag(tag: String): String {
        return tag.replace(" ","-").toLowerCase()
    }
}
