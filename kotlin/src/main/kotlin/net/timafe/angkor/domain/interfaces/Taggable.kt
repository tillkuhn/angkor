package net.timafe.angkor.domain.interfaces

/**
 * Indicates that the implementing class can hold one or multiple tags
 */
interface Taggable {

    var tags: MutableList<String>

}
