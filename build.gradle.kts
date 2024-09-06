import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    id("dev.adamko.dokkatoo-html") version "2.3.1"
    id("dev.adamko.dokkatoo-javadoc") version "2.3.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jmailen.kotlinter") version "4.4.1"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "de.smartsquare"
version = System.getenv("GITHUB_VERSION") ?: "1.0.0-SNAPSHOT"
description = "Library to emit socket.io notifications via redis."

repositories {
    mavenCentral()
}

dependencies {
    api(platform("com.fasterxml.jackson:jackson-bom:2.17.2"))
    api("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.msgpack:msgpack-core:0.9.8")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.8")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    compileOnly("redis.clients:jedis:5.1.5")
    compileOnly("io.lettuce:lettuce-core:6.4.0.RELEASE")
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis:3.3.3")

    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.amshove.kluent:kluent:1.73")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("com.redis:testcontainers-redis:2.2.2")

    testImplementation("redis.clients:jedis:5.1.5")
    testImplementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis:3.3.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        allWarningsAsErrors = true
    }
}

detekt {
    config.setFrom("${project.rootDir}/detekt.yml")

    buildUponDefaultConfig = true
}

dokkatoo {
    val jacksonVersion = resolveVersion("com.fasterxml.jackson.core:jackson-core")
    val lettuceVersion = resolveVersion("io.lettuce:lettuce-core")
    val jedisVersion = resolveVersion("redis.clients:jedis")
    val springDataRedisVersion = resolveVersion("org.springframework.boot:spring-boot-starter-data-redis")

    dokkatooSourceSets.configureEach {
        externalDocumentationLinks.create("jackson") {
            url("https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/$jacksonVersion/")
        }
        externalDocumentationLinks.create("lettuce") {
            url("https://javadoc.io/doc/io.lettuce/lettuce-core/$lettuceVersion/")
        }
        externalDocumentationLinks.create("jedis") {
            url("https://javadoc.io/doc/redis.clients/jedis/$jedisVersion/")
        }
        externalDocumentationLinks.create("spring-data-redis") {
            url("https://docs.spring.io/spring-data/redis/docs/$springDataRedisVersion/api/")
            packageListUrl("https://docs.spring.io/spring-data/redis/docs/$springDataRedisVersion/api/element-list")
        }
    }
}

fun resolveVersion(dependency: String): String {
    return project.configurations.getByName("compileClasspath").resolvedConfiguration.resolvedArtifacts
        .find { it.moduleVersion.id.module.toString() == dependency }
        ?.moduleVersion?.id?.version
        ?: "latest"
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
    configure(KotlinJvm(JavadocJar.Dokka("dokkatooGeneratePublicationJavadoc")))

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
    gradleVersion = "8.10"
}
