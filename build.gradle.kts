import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.dokka") version "1.9.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    id("org.jmailen.kotlinter") version "4.2.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("com.github.ben-manes.versions") version "0.50.0"
}

group = "de.smartsquare"
version = System.getenv("GITHUB_VERSION") ?: "1.0.0-SNAPSHOT"
description = "Library to emit socket.io notifications via redis."

repositories {
    mavenCentral()
}

dependencies {
    api(platform("com.fasterxml.jackson:jackson-bom:2.16.1"))
    api("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.msgpack:msgpack-core:0.9.7")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.7")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    compileOnly("redis.clients:jedis:5.1.0")
    compileOnly("io.lettuce:lettuce-core:6.3.1.RELEASE")
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis:2.7.18")

    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.amshove.kluent:kluent:1.73")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("com.redis:testcontainers-redis:2.0.1")

    testImplementation("redis.clients:jedis:5.1.0")
    testImplementation("io.lettuce:lettuce-core:6.3.1.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis:2.7.18")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        allWarningsAsErrors = true
    }
}

detekt {
    config.setFrom("${project.rootDir}/detekt.yml")

    buildUponDefaultConfig = true
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        externalDocumentationLink("https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/")
        externalDocumentationLink("https://javadoc.io/doc/io.lettuce/lettuce-core/latest/")
        externalDocumentationLink("https://javadoc.io/doc/redis.clients/jedis/latest/")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        excludeEngines("junit-vintage")
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        name = "Socket-io-Redis-Emitter"
        description = "Library to emit socket.io notifications via redis."
        url = "https://github.com/SmartsquareGmbH/socket-io-redis-emitter"
        inceptionYear = "2021"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
                distribution = "https://opensource.org/license/mit"
            }
        }
        developers {
            developer {
                id = "deen13"
                name = "Dennis Dierkes"
                email = "dierkes@smartsquare.de"
            }
            developer {
                id = "rubengees"
                name = "Ruben Gees"
                email = "gees@smartsquare.de"
            }
        }
        scm {
            connection = "scm:git:https://github.com/SmartsquareGmbH/socket-io-redis-emitter.git"
            developerConnection = "scm:git:ssh://github.com/SmartsquareGmbH/socket-io-redis-emitter.git"
            url = "https://github.com/SmartsquareGmbH/socket-io-redis-emitter"
        }
        organization {
            name = "Smartsquare GmbH"
            url = "https://github.com/SmartsquareGmbH"
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/SmartsquareGmbH/socket-io-redis-emitter/issues"
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.5"
}
