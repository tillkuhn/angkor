package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

/*
 * Based on https://www.java-success.com/00-%E2%99%A6-creating-tree-list-flattening-back-list-java/
 */
class TreeNode {
    var id //Current node id
            : String? = null
    var parentId //Parent node id
            : String? = null
    var value: String? = null

    @JsonIgnore
    var parent: TreeNode? = null
    private var children: MutableList<TreeNode>

    constructor() : super() {
        children = ArrayList()
    }

    constructor(value: String?, childId: String?, parentId: String?) {
        this.value = value
        id = childId
        this.parentId = parentId
        children = ArrayList()
    }

    constructor(area: Area?) {
        this.value = area?.name
        id = area?.code
        this.parentId = area?.parentCode
        children = ArrayList()
    }


    fun getChildren(): List<TreeNode> {
        return children
    }

    fun setChildren(children: MutableList<TreeNode>) {
        this.children = children
    }

    fun addChild(child: TreeNode?) {
        if (!children.contains(child) && child != null) children.add(child)
    }

    override fun toString(): String {
        return ("Node [id=" + id + ", parentId=" + parentId + ", value=" + value + ", children="
                + children + "]")
    }
}
