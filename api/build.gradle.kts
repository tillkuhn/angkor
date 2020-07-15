import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion: String by System.getProperties()
    val postgresVersion: String by System.getProperties()
    dependencies {
        classpath("org.postgresql:postgresql:$postgresVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

group = "com.github.tillkuhn"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

plugins {
    val kotlinVersion: String by System.getProperties()
    id("org.springframework.boot") version "2.3.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.flywaydb.flyway") version ("6.3.2")
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
   // maven
    jacoco
    java
}


repositories {
    mavenCentral()
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("net.logstash.logback:logstash-logback-encoder:6.2")

    // since 2.3.1 we need to add validation starter ourselves
    // https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#validation-starter-no-longer-included-in-web-starters
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin - Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    // Commons
    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.commons:commons-lang3:3.10")

    // Persistence
    val postgresVersion: String by System.getProperties()
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.flywaydb:flyway-core:6.3.2") // looks for  classpath:db/migration
    implementation("com.vladmihalcea:hibernate-types-52:2.9.11") // https://vladmihalcea.com/how-to-map-java-and-sql-arrays-with-jpa-and-hibernate/

    val jacksonVersion: String =  "2.11.1"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformats-text:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:$jacksonVersion")

    // AWS disabled
    // implementation("com.github.derjust:spring-data-dynamodb:5.1.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}


tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
    doLast {
        println("Code coverage report can be found at: file://$buildDir/reports/jacoco/test/html/index.html")
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.bootJar {
    archiveVersion.set("")
    // https://stackoverflow.com/questions/53123012/spring-boot-2-change-jar-name
    archiveFileName.set("app.jar")
}

jacoco {
    toolVersion = "0.8.5"
}

tasks.bootRun.configure {
    systemProperty("spring.profiles.active", "dev")
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
