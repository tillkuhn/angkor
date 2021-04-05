package net.timafe.angkor

import ch.qos.logback.classic.Logger
import org.junit.jupiter.api.Test

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory

class ArchUnitTests {

    private val rootPackage = "net.timafe.angkor"

    companion object {
        // Yes this is ugly ... but https://github.com/TNG/ArchUnit/issues/210
        @BeforeAll
        @JvmStatic
        fun silenceLogger() {
            val logjcp = LoggerFactory
                .getLogger("com.tngtech.archunit.core.importer.JavaClassProcessor") as Logger
            logjcp.level = ch.qos.logback.classic.Level.INFO
            val lograh = LoggerFactory
                .getLogger("com.tngtech.archunit.core.importer.ClassFileProcessor\$RecordAccessHandler") as Logger
            lograh.level = ch.qos.logback.classic.Level.INFO
            LoggerFactory.getLogger(ArchUnitTests::class.java).info("Launching ArchUnit Tests")
        }

        @AfterAll
        @JvmStatic
        fun finished() {
            LoggerFactory.getLogger(ArchUnitTests::class.java).info("Successfully Finished ArchUnit Tests")
        }
    }

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(rootPackage)

        noClasses()
            .that()
            .resideInAnyPackage("${rootPackage}.service..")
            .or()
            .resideInAnyPackage("${rootPackage}.repository..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..${rootPackage}.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses)
    }
}
