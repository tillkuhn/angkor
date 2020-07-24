package net.timafe.angkor.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * An authority (a security role) used by Spring Security.
 */
// @Entity
// @Table(name = "jhi_authority")
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class Authority(

        @field:NotNull
        @field:Size(max = 50)
        @Id
        @Column(length = 50)
        var name: String

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Authority) return false
        if (other.name == null || name == null) return false

        return name == other.name
    }

    override fun hashCode() = 31

    companion object {
        private const val serialVersionUID = 1L
    }
}
