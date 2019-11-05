package org.timafe.p2b

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import org.springframework.data.annotation.Id


@DynamoDBTable(tableName = "MyEntity")
data class MyEntity(

        @get:DynamoDBHashKey(attributeName = "group")
        var group: String? = null,

        @get:DynamoDBRangeKey(attributeName = "position")
        var position: String? = null
        //,

        //@DynamoDBAttribute(attributeName = "data")
        //@AsJson
        //var data: SomeObject? = null
) {

    @Id
    private var id: UniqueId? = null
        get() {
            return UniqueId(group, position)
        }
}

data class UniqueId(
        @DynamoDBHashKey
        var group: String? = null,
        @DynamoDBRangeKey
        var position: String? = null
)
