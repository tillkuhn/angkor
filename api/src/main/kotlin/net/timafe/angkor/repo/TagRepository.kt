package net.timafe.angkor.repo

import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Tag
import net.timafe.angkor.domain.dto.DishSummary
import net.timafe.angkor.domain.dto.TagSummary
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface TagRepository : CrudRepository<Tag, UUID> {
    companion object {
        const val MAX_TAGS_LIMIT = 42
        const val MIN_TAG_COUNT = 2
    }
    fun findByLabel(label: String): List<Tag>
    fun findByOrderByLabel(): List<Tag>

    override fun findAll(): List<Tag>

    @Query(
        value = """
        select tag as label, count(*) as count
        from place, unnest(tags) as t(tag)
        group by tag having count(*) >= :minCount order by count desc, tag asc limit :limit
    """
        , nativeQuery = true
    )
    fun placeTags(@Param("limit") limit: Int = MAX_TAGS_LIMIT,
                  @Param("minCount") minCount: Int = MIN_TAG_COUNT ): List<TagSummary>

    @Query(
        value = """
        select tag as label, count(*) as count
        from dish, unnest(tags) as t(tag)
        group by tag having count(*) > 1 order by count desc limit 25;
    """, nativeQuery = true
    )
    fun dishTags(): List<TagSummary>

    @Query(
        value = """
        select tag as label, count(*) as count
        from note, unnest(tags) as t(tag)
        group by tag having count(*) > 1 order by count desc limit 25;
    """, nativeQuery = true
    )
    fun noteTags(): List<TagSummary>

}
