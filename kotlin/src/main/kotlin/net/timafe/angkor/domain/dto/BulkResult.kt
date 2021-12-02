package net.timafe.angkor.domain.dto

/**
 * Value Holder Data class to store the result of uploads,
 * e.g. from Data Imports
 */
data class BulkResult(
    var read: Int = 0,
    var inserted: Int  = 0,
    var updated: Int  = 0,
    var deleted: Int  = 0,
    var errors: Int  = 0,
) {
    fun add(other: BulkResult) {
        read += other.read
        inserted += other.inserted
        updated  += other.updated
        deleted += other.deleted
        errors += other.errors
    }
}
