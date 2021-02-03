package net.timafe.angkor.service

import net.timafe.angkor.domain.Area
import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.repo.AreaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

@Service
class AreaService(
        private val areaRepository: AreaRepository
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    companion object {
        const val COUNTRIES_AND_REGIONS_CACHE = "countriesAndRegions"
    }

    /**
     * returns only countries and regions as a flat list
     */
    @Cacheable(COUNTRIES_AND_REGIONS_CACHE)
    fun countriesAndRegions(): List<Area> {
        val areas = areaRepository.findAllCountriesAndRegions()
        // return areaRepository.findByLevelOrderByName(AreaLevel.COUNTRY)
        log.debug("countriesAndRegions() Retrieved ${areas.size} items")
        return areas
    }

    // @CacheEvict(cacheNames="product", key ="#root.args[0].id")
    // https://www.baeldung.com/spring-cache-tutorial#2-cacheevict
    @CacheEvict(COUNTRIES_AND_REGIONS_CACHE,allEntries = true) // make sure next call to countriesAndRegions triggers reload
    fun create(item: Area): Area {
        log.debug("create() new area $item.code and evicted cache")
        return areaRepository.save(item)
    }

    @CacheEvict(cacheNames=[COUNTRIES_AND_REGIONS_CACHE])
    fun delete(item: Area) =  areaRepository.delete(item)

    /**
     * Returns area codes in a parent-child tree structure
     */
    fun getAreaTree() : List<TreeNode> {
        val treeNodes: MutableList<TreeNode> = ArrayList<TreeNode>()
        val sort: Sort = Sort.by(
                Sort.Order.asc("level"),
                Sort.Order.asc("parentCode"),
                Sort.Order.asc("name"))
        this.areaRepository.findAll(sort).forEach {
            treeNodes.add(TreeNode((it)))
        }
        //convert to a tree
        val tree =  createTree(treeNodes)
        return tree
    }

    private fun createTree(treeNodes: List<TreeNode>): List<TreeNode> {
        val mapTmp: MutableMap<String?, TreeNode> = HashMap<String?, TreeNode>()

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
        return if (root != null) root.getChildren() else listOf()
    }
}
