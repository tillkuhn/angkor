package org.timafe.angkor.model

import java.util.*

/*
 * Based on https://www.java-success.com/00-%E2%99%A6-creating-tree-list-flattening-back-list-java/
 */
class Node {
    var id //Current node id
            : String? = null
    var parentId //Parent node id
            : String? = null
    var value: String? = null
    var parent: Node? = null
    private var children: MutableList<Node>

    constructor() : super() {
        children = ArrayList()
    }

    constructor(value: String?, childId: String?, parentId: String?) {
        this.value = value
        id = childId
        this.parentId = parentId
        children = ArrayList()
    }

    fun getChildren(): List<Node> {
        return children
    }

    fun setChildren(children: MutableList<Node>) {
        this.children = children
    }

    fun addChild(child: Node?) {
        if (!children.contains(child) && child != null) children.add(child)
    }

    override fun toString(): String {
        return ("Node [id=" + id + ", parentId=" + parentId + ", value=" + value + ", children="
                + children + "]")
    }
}
