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
     */
    fun postgresArrayStringToList(arrayString: String): List<String> {
        // Todo more syntax checks
        // log.debug("Converting $arrayString")
        if (arrayString.length > 1) {
            return arrayString.subSequence(1,arrayString.length-1).split(",")
        } else {
            return listOf()
        }
    }
}
