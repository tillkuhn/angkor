import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion: String by System.getProperties()
    // val postgresVersion: String by System.getProperties()
    dependencies {
        classpath(libs.postgresql)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        // https://kotlinlang.org/docs/all-open-plugin.html#spring-support
        // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.spring
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    }
    // Customize Managed Version
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#managing-dependencies.dependency-management-plugin.customizing
    // Mitigate https://jira.qos.ch/browse/LOGBACK-1591 until it's part of Spring Boot's mainline
    extra.apply {
        // with recent spring v2.6.5, logback version is already on 1.2.11 so we no longer need this
        // set("logback.version", "1.2.8")
    }
}

group = "com.github.tillkuhn"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    val kotlinVersion: String by System.getProperties()
    // val flywayVersion: String by System.getProperties()
    // val springBootVersion: String by System.getProperties()
    val versionsVersion: String by System.getProperties()

    // https://docs.gradle.org/current/userguide/platforms.html
    // Using alias we can reference the plugin id and version
    // defined in the version catalog.
    // Notice that hyphens (-) used as separator in the identifier
    // are translated into type safe accessors for each subgroup.
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.mgmt)
    alias(libs.plugins.flyway.plugin)


    // id("org.springframework.boot") version springBootVersion
    // id("io.spring.dependency-management") version "1.1.3"
    //id("org.flywaydb.flyway") version flywayVersion
    // Plugin to determine which dependencies have updates, including updates for gradle itself.
    id("com.github.ben-manes.versions") version versionsVersion
    // Gradle plugin for running SonarQube analysis. https://plugins.gradle.org/plugin/org.sonarqube
    id("org.sonarqube") version "4.4.1.3373" // "4.4.1.3373" //  "4.0.0.2929" // new ones has issues

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    // The no-arg compiler plugin generates an additional zero-argument constructor for classes with a specific annotation.
    // (...)  is synthetic so it can’t be directly called from Java or Kotlin, but it can be called using reflection.
    // https://kotlinlang.org/docs/no-arg-plugin.html
    // As with the kotlin-spring plugin wrapped on top of all-open, kotlin-jpa is wrapped on top of no-arg.
    // The plugin specifies @Entity, @Embeddable, and @MappedSuperclass no-arg annotations automatically.
    // kotlin("plugin.noarg") version kotlinVersion
    // https://kotlinlang.org/docs/all-open-plugin.html not needed, kotlin-spring compiler plugin will handle that
    // kotlin("plugin.allopen") version kotlinVersion

    jacoco // The builtin Gradle plugin implemented by org.gradle.testing.jacoco.plugins.JacocoPlugin.
    java // The builtin Gradle plugin implemented by org.gradle.api.plugins.JavaPlugin.

}

repositories {
    // A repository which looks in the Maven central repository for dependencies.
    mavenCentral()
    // JitPack is a novel package repository for JVM and Android projects.
    // It builds Git projects on demand and provides you with ready-to-use artifacts (jar, aar).
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Spring, SpringBoot and associated Starter Kits
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.kafka:spring-kafka")

    // Sometimes ... caching makes sense: https://codeboje.de/caching-spring-boot/
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Add validation starter explicitly (required since 3.1)
    // https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#validation-starter-no-longer-included-in-web-starters
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin - Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    // Commons, HTTP Client, RSS and other Network Communication Stuff
    val unirestVersion: String by System.getProperties()
    val commonsLangVersion: String by System.getProperties()
    val romeVersion: String by System.getProperties()
    val bucket4jVersion: String by System.getProperties()
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
    implementation("com.mashape.unirest:unirest-java:$unirestVersion")
    implementation ("com.rometools:rome:$romeVersion")
    implementation ("com.rometools:rome-modules:$romeVersion")
    // https://mvnrepository.com/artifact/com.github.vladimir-bukhtoyarov/bucket4j-core
    implementation ("com.github.vladimir-bukhtoyarov:bucket4j-core:$bucket4jVersion")


    // Persistence (Postgres, JPA, Hibernate)
    // val postgresVersion: String by System.getProperties()
    // val flywayVersion: String by System.getProperties()
    val hypersistenceUtilsVersion: String by System.getProperties()
    implementation(libs.postgresql)
    implementation(libs.flyway.core)

    // implementation("org.flywaydb:flyway-core:$flywayVersion") // looks for  classpath:db/migration
    implementation("io.hypersistence:hypersistence-utils-hibernate-62:$hypersistenceUtilsVersion") // https://vladmihalcea.com/how-to-map-java-and-sql-arrays-with-jpa-and-hibernate/

    // Jackson JSON Parsing Dependencies
    // For Gradle users, if you use the Spring Boot Gradle plugin you can omit the version number to adopt
    // the dependencies managed by Spring Boot, such as those Jackson modules
    // https://stackoverflow.com/questions/25184556/how-to-make-sure-spring-boot-extra-jackson-modules-are-of-same-version
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Test Dependencies
    val archUnitVersion: String by System.getProperties()
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // https://stackoverflow.com/a/52980523/4292075
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    val mockitoInlineVersion: String by System.getProperties()
    val wiremockVersion: String by System.getProperties()
    val greenmailVersion: String by System.getProperties()
    // Mockito Inline required to mock final classes (https://stackoverflow.com/a/14292888/4292075)
    testImplementation("org.mockito:mockito-inline:$mockitoInlineVersion")
    testImplementation( "com.github.tomakehurst:wiremock:$wiremockVersion")
    testImplementation("com.tngtech.archunit:archunit-junit5-api:$archUnitVersion")
    testImplementation("com.icegreen:greenmail:$greenmailVersion")
    testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:$archUnitVersion")

}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
    doLast {
        println("Code coverage report at: file://$buildDir/reports/jacoco/test/html/index.html")
    }
}

tasks.withType<KotlinCompile> {
    // The strict value is required to have null-safety taken in account in Kotlin types inferred
    // from Spring API: https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/boot-features-kotlin.html
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

// Ensure predictable jar name "app.jar" (comes in handy in Dockerfile)
// Source: https://stackoverflow.com/questions/53123012/spring-boot-2-change-jar-name
tasks.bootJar {
    archiveVersion.set("")
    archiveFileName.set("app.jar")
}

jacoco {
    toolVersion = "0.8.7"
}

// Configure which reports are generated by Jacococ coverage tool
// https://kevcodez.de/posts/2018-08-19-test-coverage-in-kotlin-with-jacoco/
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.bootRun.configure {
    systemProperty("spring.profiles.active", "default")
}

// Custom tasks for different spring profiles...
tasks.register("bootRunClean") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the clean db profile"
    doFirst {
        tasks.bootRun.configure {
            systemProperty("spring.profiles.active", "clean")
        }
    }
    finalizedBy("bootRun")
}

tasks.register("bootRunProd") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the prod profile"
    doFirst {
        tasks.bootRun.configure {
            systemProperty("spring.profiles.active", "prod")
        }
    }
    finalizedBy("bootRun")
}

// Helper function to decide which versions to consider if we run dependencyUpdates task
// https://github.com/ben-manes/gradle-versions-plugin#tasks
// disallow release candidates as upgradable versions from stable versions
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "angkor-api")
        property("sonar.projectName", "Angkor API")
        property("sonar.projectDescription", "API for Angular Kotlin Rest App")
        property("sonar.coverage.jacoco.xmlReportPaths","build/reports/jacoco/test/jacocoTestReport.xml")
        // domain objects are mostly data classes which don't support inheritance really well, so we exlude
        // them from duplication detection (cf. https://docs.sonarqube.org/7.4/analysis/analysis-parameters/)
        property("sonar.cpd.exclusions","src/main/kotlin/net/timafe/angkor/domain/**/*")
    }
}
