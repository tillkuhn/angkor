package net.timafe.angkor.domain.dto

data class MetricDTO (
        var name: String,
        var description: String?,
        var value: String?,
        var baseUnit: String?
)

/* Example metric repsponse
"name" : "process.uptime",
"description" : "The uptime of the Java virtual machine",
"baseUnit" : "seconds",
"measurements" : [ {
    "statistic" : "VALUE",
    "value" : 19.572
} ],
"availableTags" : [ ]

 */
