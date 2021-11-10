package net.timafe.angkor.domain

import com.fasterxml.jackson.databind.ObjectMapper
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.security.SecurityUtils
import kotlin.collections.set
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * https://www.java-success.com/00-%E2%99%A6-creating-tree-list-flattening-back-list-java/
 */
class TreeNodeUT {

    @Test
    fun testScopeBuilder() {
        // https://itnext.io/how-to-map-postgresql-array-type-to-list-using-jpa-and-eclipselink-b4e25ca13490
        val scopes = listOf(AuthScope.PUBLIC,AuthScope.PRIVATE)
        val scopesStr = SecurityUtils.authScopesAsString(scopes)
        assertEquals("{\"PUBLIC\", \"PRIVATE\"}",scopesStr)
        //println(scopesStr) // scopesStr
    }

    @Test
    fun testTree() {

        //Create a List of nodes
        val treeNodes: MutableList<TreeNode> = ArrayList()
        treeNodes.add(TreeNode(Area("europe","Europe","world")))
        treeNodes.add(TreeNode(Area("de","Germany","europe")))
        treeNodes.add(TreeNode(Area("de-by","Germany Bayern","de")))
        treeNodes.add(TreeNode(Area("de-nw","Germany NRW","de")))
        treeNodes.add(TreeNode(Area("it","Italy","europe")))
        //convert to a tree
        val tree = createTree(treeNodes)
        // expected, actual, message
        assertEquals(2, tree?.getChildren()?.size, "expected 5 children")
        println(ObjectMapper().writeValueAsString(tree))
    }

    private fun createTree(treeNodes: List<TreeNode>): TreeNode? {
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
            if (node.parent == null) {
                root = node
                break
            }
        }
        return root
    }
}

/*
BW = Baden-Württemberg;
BY = Bayern;
BE = Berlin;
BB = Brandenburg;
HB = Bremen;
HH = Hamburg;
HE = Hessen;
MV = Mecklenburg-Vorpommern;
NI = Niedersachsen;
NW = Nordrhein-Westfalen;
RP = Rheinland-Pfalz;
SL = Saarland;
SN = Sachsen;
ST = Sachsen-Anhalt;
SH = Schleswig-Holstein;
TH = Thüringen.
 */
