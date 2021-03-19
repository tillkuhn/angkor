package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.repo.AreaRepository
import net.timafe.angkor.repo.AreaRepository.Companion.COUNTRIES_AND_REGIONS_CACHE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
/**
 * Service to manage [Area]
 */
@Service
@Transactional
class AreaService(
    private val areaRepository: AreaRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional(readOnly = true)
    fun findOne(id: String): Optional<Area> {
        val item = areaRepository.findById(id)
        log.debug("[AREA] FindOne: id=$id found=${item.isPresent}")
        return item
    }

    @Transactional(readOnly = true)
    fun allAreas(): List<Area> {
        return  areaRepository.findByOrderByName()
    }

    /**
     * returns only countries and regions as a flat list
     * use areaRepository.findByLevelOrderByName(AreaLevel.COUNTRY)
     * to filter by COUNTRY only
     */
    @Transactional(readOnly = true)
    fun countriesAndRegions(): List<Area> {
        val areas = areaRepository.findAllCountriesAndRegions()
        log.debug("countriesAndRegions() Retrieved ${areas.size} items")
        return areas
    }

    @CacheEvict(cacheNames = [COUNTRIES_AND_REGIONS_CACHE], allEntries = true)
    fun save(item: Area): Area {
        log.debug("create() new area $item.code and evicted $COUNTRIES_AND_REGIONS_CACHE")
        return areaRepository.save(item)
    }

    @CacheEvict(cacheNames = [COUNTRIES_AND_REGIONS_CACHE], allEntries = true)
    fun delete(id: String) = areaRepository.deleteById(id)

    /**
     * Returns area codes in a parent-child tree structure
     */
    @Transactional(readOnly = true)
    fun getAreaTree(): List<TreeNode> {
        val treeNodes: MutableList<TreeNode> = ArrayList<TreeNode>()
        val sort: Sort = Sort.by(
            Sort.Order.asc("level"),
            Sort.Order.asc("parentCode"),
            Sort.Order.asc("name")
        )
        this.areaRepository.findAll(sort).forEach {
            treeNodes.add(TreeNode((it)))
        }
        //convert to a tree
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

}
