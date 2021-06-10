package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.domain.enums.EntityType
import net.timafe.angkor.repo.AreaRepository
import net.timafe.angkor.repo.AreaRepository.Companion.COUNTRIES_AND_REGIONS_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.set

/**
 * Service to manage [Area]
 */
@Service
@Transactional
class AreaService(
    private val repo: AreaRepository
) : AbstractEntityService<Area, Area, String>(repo) {

    /**
     * returns only countries and regions as a flat list
     * use areaRepository.findByLevelOrderByName(AreaLevel.COUNTRY)
     * to filter by COUNTRY only
     */
    @Transactional(readOnly = true)
    fun countriesAndRegions(): List<Area> {
        val areas = repo.findAllCountriesAndRegions() // uses COUNTRIES_AND_REGIONS_CACHE
        log.debug("${super.logPrefix()} countriesAndRegions() Retrieved ${areas.size} items")
        return areas
    }

    // Delegate, but use function as holder for cache annotation
    @CacheEvict(cacheNames = [COUNTRIES_AND_REGIONS_CACHE], allEntries = true)
    override fun save(item: Area): Area = super.save(item)

    // Delegate, but use function as holder for cache annotation
    @CacheEvict(cacheNames = [COUNTRIES_AND_REGIONS_CACHE], allEntries = true)
    override fun delete(id: String) = super.delete(id)

    /**
     * Returns area codes in a parent-child tree structure
     */
    @Transactional(readOnly = true)
    fun getAreaTree(): List<TreeNode> {
        val treeNodes: MutableList<TreeNode> = ArrayList()
        val sort: Sort = Sort.by(
            Sort.Order.asc("level"),
            Sort.Order.asc("parentCode"),
            Sort.Order.asc("name")
        )
        this.repo.findAll(sort).forEach {
            treeNodes.add(TreeNode((it)))
        }
        return buildTree(treeNodes)
    }

    /**
     * Build up a tree of areas using parent/child relationships
     */
    private fun buildTree(treeNodes: List<TreeNode>): List<TreeNode> {
        val mapTmp: MutableMap<String?, TreeNode> = HashMap()

        //Save all nodes to a map
        for (current in treeNodes) {
            mapTmp[current.id] = current
        }

        //loop and assign parent/child relationships
        for (current in treeNodes) {
            val parentId: String? = current.parentId
            if (parentId != null) {
                val parent: TreeNode? = mapTmp[parentId]
                if (parent != null) {
                    current.parent = parent
                    parent.addChild(current)
                    mapTmp[parentId] = parent
                    mapTmp[current.id] = current
                }
            }
        }

        //get the root
        var root: TreeNode? = null
        for (node in mapTmp.values) {
            if (node.parentId == null) {
                root = node
                break
            }
        }
        return root?.getChildren() ?: listOf()
    }


    // impl required by superclass
    override fun entityType(): EntityType {
        return EntityType.AREA
    }
}
