package net.timafe.angkor.domain

import com.fasterxml.jackson.annotation.JsonFormat
import net.timafe.angkor.config.Constants
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
@Table(name = "app_user")
@TypeDef(
        name = "list-array",
        typeClass = com.vladmihalcea.hibernate.type.array.ListArrayType::class
)
data class User(

        @Id
        var id: String? = null,

        @field:NotBlank
        // @field:Pattern(regexp = LOGIN_REGEX)
        @field:Size(min = 1, max = 50)
        var login: String? = null,

        @field:Size(max = 50)
        var firstName: String? = null,

        @field:Size(max = 50)
        var lastName: String? = null,

        @field:Size(max = 50)
        var name: String? = null,

        @field:Email
        @field:Size(min = 5, max = 254)
        var email: String? = null,

        @field:Size(max = 256)
        var imageUrl: String? = null,

        var activated: Boolean = false,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var createdAt: LocalDateTime? = LocalDateTime.now(),

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var updatedAt: LocalDateTime? = LocalDateTime.now(),

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.JACKSON_DATE_FORMAT)
        var lastLogin: LocalDateTime? = null,

        @Type(type = "list-array")
        @Column(
                name = "roles",
                columnDefinition = "text[]"
        )
        var roles: List<String> = listOf()

) {

}
