package net.timafe.angkor.config.annotations

import net.timafe.angkor.domain.enums.EntityType

/**
 * Annotation which allows us to derive the [EntityType] enum value from
 * a properly annotated instance of some entity class
 *
 * See also:
 * https://www.baeldung.com/kotlin/annotations
 * https://discuss.kotlinlang.org/t/finding-field-annotations-using-obj-class-declaredmemberproperties/8497
 *
 * For Field Targets:
 * "Kotlin’s property get compiled to a getter and a backing field on the JVM. If you try to get the annotations of the
 * property, you don’t get the annotation of the underlying field. Your code would find the annotations if you changed
 * the target from FIELD to property"
 */
// Possible Annotation Targets: Class, interface or object, annotation class is also included
@Target(AnnotationTarget.CLASS)
// Annotation is stored in binary output and visible for reflection (default retention)
@Retention(AnnotationRetention.RUNTIME)
annotation class EntityTypeInfo(
    val type: EntityType,
    val eventOnCreate: Boolean = false,
    val eventOnUpdate: Boolean = false,
    val eventOnDelete: Boolean = false,
)
