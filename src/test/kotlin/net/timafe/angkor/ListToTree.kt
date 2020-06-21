package net.timafe.angkor

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set
import org.timafe.angkor.model.Node
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * Tree to be created
 * 1 --> 2
 * --> 3
 * --> 4 --> 5
 *
 */
class ListToTree {

    @Test
    fun testTree() {

        //Create a List of nodes
        val nodes: MutableList<Node> = ArrayList<Node>()
        nodes.add( Node("Five", "5", "4"))
        nodes.add(Node("Four", "4", "2"))
        nodes.add( Node("Two", "2", "1"))
        nodes.add( Node("Three", "3", "2"))
        nodes.add(Node("One", "1", null) )
        nodes.add(Node("TwoHalf", "9", "1"))
        //convert to a tree
        var tree = createTree(nodes)
        // expected, actual, message
        assertEquals(2, tree?.getChildren()?.size, "expected 5 children" )
        //System.out.println(tree)
    }

    private fun createTree(nodes: List<Node>): Node? {
        val mapTmp: MutableMap<String?, Node> = HashMap<String?, Node>()

        //Save all nodes to a map
        for (current in nodes) {
            mapTmp[current.id] = current
        }

        //loop and assign parent/child relationships
        for (current in nodes) {
            val parentId: String? = current.parentId
            if (parentId != null) {
                val parent: Node? = mapTmp[parentId]
                if (parent != null) {
                    current.parent = parent
                    parent.addChild(current)
                    mapTmp[parentId] = parent
                    mapTmp[current.id] = current
                }
            }
        }


        //get the root
        var root: Node? = null
        for (node in mapTmp.values) {
            if (node.parent == null) {
                root = node
                break
            }
        }
        return root
    }
}
