package net.timafe.angkor.domain.dto

data class MetricDTO (
        var name: String,
        var description: String?,
        var value: Any?,
        var baseUnit: String?
        ) {
        // System.out.printf("dexp: %.0f\n", dexp);
        val valueFormatted: String?
                get() = if (value is Double) String.format("%.0f",value) else value?.toString() + "hase"
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
