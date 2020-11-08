package net.timafe.angkor.service

import net.timafe.angkor.domain.TreeNode
import net.timafe.angkor.repo.AreaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
