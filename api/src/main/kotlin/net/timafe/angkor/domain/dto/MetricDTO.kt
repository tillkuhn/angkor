package net.timafe.angkor.domain.dto

data class MetricDTO(
    val name: String,
    val description: String?,
    val value: Any?,
    val baseUnit: String?
) {
    val valueFormatted: String?
        get() = if (value is Double) String.format("%.0f", value) else value?.toString()
}

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
