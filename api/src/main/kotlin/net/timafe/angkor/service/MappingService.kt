package net.timafe.angkor.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MappingService {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Useful for native queries which bypass our custom array mappers
     * converts strings like {veggy,spicy} to kotlin arrays
     * avoids "No Dialect mapping for JDBC type: 2003" error
     */
    fun postgresArrayStringToList(arrayString: String): List<String> {
        // Todo more syntax checks
        // log.debug("Converting $arrayString")
        if (arrayString.length > 2) { // {} is empty
            return arrayString.subSequence(1, arrayString.length - 1).split(",")
        } else {
            return listOf()
        }
    }

    fun postgresCoordinateStringToList(arrayString: String): List<Double> {
        // Todo more syntax checks
        // log.debug("Converting $arrayString")
        if (arrayString.length > 2) { // {} is empty
            return arrayString.subSequence(1, arrayString.length - 1)
                .split(",")
                .map { it.toDouble() }
        } else {
            return listOf()
        }
    }

}
