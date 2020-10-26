package net.timafe.angkor.rest

import net.timafe.angkor.config.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.nio.file.*

import java.util.*

/**
 * https://medium.com/linkit-intecs/file-upload-download-as-multipart-file-using-angular-6-spring-boot-7ad06d841c21
 */
@Controller
@RequestMapping(Constants.API_DEFAULT_VERSION)
class FileUploader {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    var files: MutableList<String?> = ArrayList()
    //private val rootLocation: Path = Paths.get("_Path_To_Save_The_File")

    @PostMapping(Constants.API_PATH_PLACES + "/{id}/upload")
    fun uploadPlaceFile(@PathVariable id: String, @RequestParam("file") file: MultipartFile): ResponseEntity<String?>? {
        var message: String
        var status: HttpStatus
        try {
            val tmpDir = System.getProperty("java.io.tmpdir")
            val storeDir = Files.createDirectories(Paths.get("/$tmpDir/${Constants.API_PATH_PLACES}/$id"));
            val writtenBytes = Files.copy(file.inputStream, storeDir.resolve(file.originalFilename),StandardCopyOption.REPLACE_EXISTING)
            message = "Successfully uploaded $writtenBytes bytes to $storeDir/${file.originalFilename}"
            files.add(file.originalFilename)
            status = HttpStatus.OK
            log.info(message)
        } catch (e: Exception) {
            status = HttpStatus.EXPECTATION_FAILED // 417
            message = "Failed to upload!" + e.message
            log.error(message)
        }
        return ResponseEntity.status(status).body(message)
    }

}
