package net.timafe.angkor

import ch.qos.logback.classic.Logger
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * "ArchUnit is a free, simple and extensible library for checking the architecture of your
 * Java code using any plain Java unit test framework."
 *
 * https://www.archunit.org/
 *
 */
class ArchUnitTests {

    private val rootPackage = "net.timafe.angkor"
    private  val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(rootPackage)

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
    fun `services, domain and config Should Not Depend On Web Layer`() {


        // Package Dependency Checks
        // The two dots (..) represent any number of packages (compare AspectJ Pointcuts).
        // https://www.archunit.org/userguide/html/000_Index.html#_package_dependency_checks
        noClasses()
            .that()
            .resideInAnyPackage("${rootPackage}.config..")
            .or()
            .resideInAnyPackage("${rootPackage}.domain..")
            .or()
            .resideInAnyPackage("${rootPackage}.service..")
            .or()
            .resideInAnyPackage("${rootPackage}.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..${rootPackage}.web..")
            .because("Services, repositories and domain objects should not depend on web layer")
            .check(importedClasses)

    }
    @Test
    fun `only service and controller should depend on repo layer`() {
        layeredArchitecture()
            .layer("Repo").definedBy("..repo..")
            .layer("Domain").definedBy("..domain..")
            .layer("Service").definedBy("..service..")
            .layer("Controller").definedBy("..web..")
            .whereLayer("Repo").mayOnlyBeAccessedByLayers("Service","Controller")
            .check(importedClasses)
    }

    @Test
    fun `check if we are free of circles`() {
        slices().matching("${rootPackage}.(*)..").should().beFreeOfCycles()
    }
}
