package net.timafe.angkor.domain.interfaces

/**
 * Indicates that the implementing class holds coordinates
 * and can act as a PoI
 */
interface Mappable {

    var coordinates: List<Double> // abstract property

}
