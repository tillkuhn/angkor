package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonIgnore

/*
 * Based on https://www.java-success.com/00-%E2%99%A6-creating-tree-list-flattening-back-list-java/
 */
class TreeNode(area: Area?) {
    var id: String? = null // Current node id
    var parentId: String? = null // Parent node id
    var value: String? = null

    @JsonIgnore
    var parent: TreeNode? = null
    private var children: MutableList<TreeNode>

    init {
        this.value = area?.name
        id = area?.code
        this.parentId = area?.parentCode
        children = ArrayList()
    }


    fun getChildren(): List<TreeNode> {
        return children
    }

    fun addChild(child: TreeNode?) {
        if (!children.contains(child) && child != null) children.add(child)
    }

    override fun toString(): String {
        return ("Node [id=" + id + ", parentId=" + parentId + ", value=" + value + ", children="
                + children + "]")
    }
}
