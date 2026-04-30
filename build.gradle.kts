import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.versions)
}

group = "de.smartsquare"
version = System.getenv("GITHUB_VERSION") ?: "1.0.0-SNAPSHOT"
description = "Library to emit socket.io notifications via redis."

repositories {
    mavenCentral()
}

dependencies {
    api(platform(libs.jackson))
    api(libs.jackson.databind)

    implementation(libs.msgpack.core)
    implementation(libs.msgpack.jackson)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.jsr310)

    compileOnly(libs.jedis)
    compileOnly(libs.lettuce.core)
    compileOnly(libs.spring.boot.starter.data.redis)

    testImplementation(libs.mockk)
    testImplementation(libs.kluent)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jsonassert)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.redis)

    testImplementation(libs.jedis)
    testImplementation(libs.lettuce.core)
    testImplementation(libs.spring.boot.starter.data.redis)

    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        allWarningsAsErrors = true
    }
}

detekt {
    config.setFrom("${project.rootDir}/detekt.yml")

    buildUponDefaultConfig = true
}

dokka {
    dokkaSourceSets.configureEach {
        sourceLink { remoteUrl("https://github.com/SmartsquareGmbh/socket-io-redis-emitter/tree/main") }

        externalDocumentationLinks.register("jackson") {
            url(libs.versions.jackson.map { "https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/$it" })
            packageListUrl(url.map { it.resolve("${it.path}/package-list").normalize().toString() })
        }
        externalDocumentationLinks.register("lettuce") {
            url(libs.versions.lettuce.map { "https://javadoc.io/doc/io.lettuce/lettuce-core/$it" })
            packageListUrl(url.map { it.resolve("${it.path}/package-list").normalize().toString() })
        }
        externalDocumentationLinks.register("jedis") {
            url(libs.versions.jedis.map { "https://javadoc.io/doc/redis.clients/jedis/$it" })
            packageListUrl(url.map { it.resolve("${it.path}/package-list").normalize().toString() })
        }
        externalDocumentationLinks.register("spring-data-redis") {
            url(libs.versions.spring.boot.map { "https://docs.spring.io/spring-data/redis/docs/$it/api" })
            packageListUrl(url.map { it.resolve("${it.path}/element-list").normalize().toString() })
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
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
    configure(KotlinJvm(JavadocJar.Dokka(tasks.dokkaGeneratePublicationJavadoc.name)))

    publishToMavenCentral(automaticRelease = true)
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
                organization = "Smartsquare GmbH"
                organizationUrl = "https://github.com/SmartsquareGmbH"
            }
            developer {
                id = "rubengees"
                name = "Ruben Gees"
                email = "gees@smartsquare.de"
                organization = "Smartsquare GmbH"
                organizationUrl = "https://github.com/SmartsquareGmbH"
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
    gradleVersion = libs.versions.gradle.get()
}
