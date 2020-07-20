package net.timafe.angkor

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set
import net.timafe.angkor.domain.TreeNode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * https://www.java-success.com/00-%E2%99%A6-creating-tree-list-flattening-back-list-java/
 */
class ListToTree {

    @Test
    fun testTree() {

        //Create a List of nodes
        val treeNodes: MutableList<TreeNode> = ArrayList<TreeNode>()
        treeNodes.add( TreeNode("Five", "5", "4"))
        treeNodes.add(TreeNode("Four", "4", "2"))
        treeNodes.add( TreeNode("Two", "2", "1"))
        treeNodes.add( TreeNode("Three", "3", "2"))
        treeNodes.add(TreeNode("One", "1", null) )
        treeNodes.add(TreeNode("TwoHalf", "9", "1"))
        //convert to a tree
        var tree = createTree(treeNodes)
        // expected, actual, message
        assertEquals(2, tree?.getChildren()?.size, "expected 5 children" )
        //System.out.println(tree)
    }

    private fun createTree(treeNodes: List<TreeNode>): TreeNode? {
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
            if (node.parent == null) {
                root = node
                break
            }
        }
        return root
    }
}
