package net.timafe.angkor.domain

import javax.persistence.*

@Entity
data class Geocode(

        // https://vladmihalcea.com/uuid-identifier-jpa-hibernate/
        @Id
        var code: String?,

        var name: String,
        var parentCode: String

)

