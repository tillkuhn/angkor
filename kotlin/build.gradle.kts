import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath(libs.postgresql)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.kotlin.all.open) // https://kotlinlang.org/docs/all-open-plugin.html#spring-support
    }
    // use extra.apply block to customize / overwrite derived versions
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#managing-dependencies.dependency-management-plugin.customizing
    extra.apply {
        // Mitigate https://jira.qos.ch/browse/LOGBACK-1591 until it's part of Spring Boot's mainline
        // with recent spring v2.6.5, logback version is already on 1.2.11 so we no longer need this
        // set("logback.version", "1.2.8")
    }
}

group = "com.github.tillkuhn"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

plugins {

    // Central declaration of dependencies: https://docs.gradle.org/current/userguide/platforms.html
    // Using alias we can reference the plugin id and version defined in the version catalog.
    // Hyphens (-) used as separator in the identifier are translated into type safe accessors for each subgroup.
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dep.mgmt)
    alias(libs.plugins.flyway.plugin)

    // Plugin to determine which dependencies have updates, including updates for gradle itself.
    alias(libs.plugins.versions)

    // Gradle plugin for running SonarQube analysis. https://plugins.gradle.org/plugin/org.sonarqube
    // id("org.sonarqube") version "4.3.1.3277" // new ones may cause issues against sonarcloud.io, so test first
    alias(libs.plugins.sonarqube)

    val kotlinVersion = libs.versions.kotlin.get()
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    // The no-arg compiler plugin generates an additional zero-argument constructor for classes with a specific annotation.
    // (...)  is synthetic, so it can’t be directly called from Java or Kotlin, but it can be called using reflection.
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
    // Spring Boot and associated Starter Kits
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.cache) // https://codeboje.de/caching-spring-boot/
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.json)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.validation)  // Add validation starter explicitly (required since 3.1)
    implementation(libs.spring.boot.starter.web)

    // Kafka Client Support
    implementation(libs.spring.kafka)

    // Kotlin - Use the Kotlin JDK 8 standard library.
    val kotlinVersion = libs.versions.kotlin.get()
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation(kotlin("stdlib-jdk8",kotlinVersion))
    implementation(kotlin("reflect",kotlinVersion))
    testImplementation(kotlin("test",kotlinVersion))
    testImplementation(kotlin("test-junit5",kotlinVersion))

    // Commons, HTTP Client, RSS and other Network Communication Stuff
    implementation(libs.commons.lang3)
    implementation(libs.unirest) {
        // avoid Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath
        //  in order to avoid potential conflicts (https://stackoverflow.com/a/77605594/4292075)
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.bundles.rome)
    implementation(libs.bucket4j)

    // Persistence (Postgres, JPA, Hibernate)
    implementation(libs.postgresql)
    implementation(libs.bundles.flyway)
    implementation(libs.hypersistence.utils.hibernate)
    
    // Monitoring / Micrometer
    implementation(libs.micrometer.prometheus)

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
    testImplementation(libs.spring.boot.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // https://stackoverflow.com/a/52980523/4292075
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.kafka.test)
    // Mockito Inline required to mock final classes (https://stackoverflow.com/a/14292888/4292075)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.wiremock)
    testImplementation(libs.archunit.api)
    testImplementation(libs.greenmail)
    testRuntimeOnly(libs.archunit.engine)

}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
    doLast {
        println("Code coverage report at: file://${layout.buildDirectory.get()}/reports/jacoco/test/html/index.html")
    }
}

tasks.withType<KotlinCompile> {
    // The strict value is required to have null-safety taken in account in Kotlin types inferred
    // from Spring API: https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/boot-features-kotlin.html
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        // align jvmTarget value with java.sourceCompatibility in Top Section!
        jvmTarget = "21"
    }
}

// Ensure predictable jar name "app.jar" (comes in handy in Dockerfile)
// Source: https://stackoverflow.com/questions/53123012/spring-boot-2-change-jar-name
tasks.bootJar {
    archiveVersion.set("")
    archiveFileName.set("app.jar")
}

jacoco {
    // why get()? See https://github.com/gradle/gradle/issues/20392
    toolVersion = libs.versions.jacoco.get()
}

// Configure which reports are generated by Jacoco coverage tool
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
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
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
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        // domain objects are mostly data classes which don't support inheritance really well, so we exclude
        // them from duplication detection (cf. https://docs.sonarqube.org/7.4/analysis/analysis-parameters/)
        property("sonar.cpd.exclusions", "src/main/kotlin/net/timafe/angkor/domain/**/*")
        // The 'sonarqube' task depends on compile tasks. This behavior is now deprecated and will be removed in version 5.x.
        // To avoid implicit compilation, set property 'sonar.gradle.skipCompile' to 'true' and make sure your project is compiled, before analysis has started.
        // BUT: https://community.sonarsource.com/t/sonar-gradle-skipcompile-is-not-working/102710/4
        property("sonar.gradle.skipCompile", "true")
    }
}
