package net.timafe.angkor.config.annotations

import net.timafe.angkor.domain.enums.EntityType

/**
 * https://www.baeldung.com/kotlin/annotations
 * https://discuss.kotlinlang.org/t/finding-field-annotations-using-obj-class-declaredmemberproperties/8497
 * For Field Targets:
 * "Kotlin’s property get compiled to a getter and a backing field on the JVM. If you try to get the annotations of the
 * property, you don’t get the annotation of the underlying field. Your code would find the annotations if you changed
 * the target from FIELD to property"
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagedEntity(val entityType: EntityType)
