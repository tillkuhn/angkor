package net.timafe.angkor.repo

import net.timafe.angkor.domain.Tag
import net.timafe.angkor.domain.dto.TagSummary
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface TagRepository : CrudRepository<Tag, UUID> {

    companion object {
        const val MAX_TAGS_LIMIT = 42
        const val MIN_TAG_COUNT = 2
        const val GROUP_ORDER_CLAUSE =
            "group by tag having count(*) >= :minCount order by count desc, tag asc limit :limit"
        const val TAGS_FOR_DISHES_CACHE: String = "tagsForDishes"
        const val TAGS_FOR_NOTES_CACHE: String = "tagsForNotes"
        const val TAGS_FOR_PLACES_CACHE: String = "tagsForPlaces"
    }

    fun findByLabel(label: String): List<Tag>
    fun findByOrderByLabel(): List<Tag>

    override fun findAll(): List<Tag>

    @Query(
        value = """
        select tag as label, count(*) as count
        from place, unnest(tags) as t(tag)
    """ + GROUP_ORDER_CLAUSE, nativeQuery = true
    )
    @Cacheable(cacheNames = [TAGS_FOR_PLACES_CACHE])
    fun findTagsForPlaces(
        @Param("limit") limit: Int = MAX_TAGS_LIMIT,
        @Param("minCount") minCount: Int = MIN_TAG_COUNT
    ): List<TagSummary>

    @Query(
        value = """
        select tag as label, count(*) as count
        from dish, unnest(tags) as t(tag)
    """ + GROUP_ORDER_CLAUSE, nativeQuery = true
    )
    @Cacheable(cacheNames = [TAGS_FOR_DISHES_CACHE])
    fun findTagsForDishes(
        @Param("limit") limit: Int = MAX_TAGS_LIMIT,
        @Param("minCount") minCount: Int = MIN_TAG_COUNT
    ): List<TagSummary>

    @Query(
        value = """
        select tag as label, count(*) as count
        from note, unnest(tags) as t(tag)
    """ + GROUP_ORDER_CLAUSE, nativeQuery = true
    )
    @Cacheable(cacheNames = [TAGS_FOR_NOTES_CACHE])
    fun findTagsForNotes(
        @Param("limit") limit: Int = MAX_TAGS_LIMIT,
        @Param("minCount") minCount: Int = MIN_TAG_COUNT
    ): List<TagSummary>

}
