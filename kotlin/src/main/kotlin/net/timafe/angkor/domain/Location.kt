package net.timafe.angkor.domain

import java.util.*
import javax.persistence.*
import java.io.Serializable

@Entity
@Table(name = "location")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    discriminatorType = DiscriminatorType.STRING,
    name = "ltype"
)
// https://jivimberg.io/blog/2018/11/05/using-uuid-on-spring-data-jpa-entities/
open class Location(
    @Id
    open var id: UUID = UUID.randomUUID(),

    @Version
    open var version: Long? = null,

    open var name: String? = "dummy",
): Serializable {

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is Location -> false
            else -> id == other.id
        }
    }


    override fun toString() =
        "Location{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}

//    override fun equals(other: Any?) = when {
//        this === other -> true
//        javaClass != other?.javaClass -> false
//        id != (other as Location).id -> false
//        else -> true
//    }
//
//    override fun hashCode(): Int = id.hashCode()
//}
