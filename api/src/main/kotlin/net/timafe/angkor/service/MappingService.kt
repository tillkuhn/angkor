package net.timafe.angkor.service

import org.springframework.stereotype.Service

/**
 * Service is mainly used in context with native queries
 * should add more syntax checks here
 */
@Service
class MappingService {

    /**
     * Useful for native queries which bypass our custom array mappers
     * converts strings like {veggy,spicy} to kotlin arrays
     * avoids "No Dialect mapping for JDBC type: 2003" error
     *
     */
    fun postgresArrayStringToList(arrayString: String): List<String> {
        return if (arrayString.length > 2) { // {} is empty
            arrayString.subSequence(1, arrayString.length - 1).split(",")
        } else {
            listOf()
        }
    }

    fun postgresCoordinateStringToList(arrayString: String): List<Double> {
        return if (arrayString.length > 2) { // {} is empty
            arrayString.subSequence(1, arrayString.length - 1)
                .split(",")
                .map { it.toDouble() }
        } else {
            listOf()
        }
    }

}
