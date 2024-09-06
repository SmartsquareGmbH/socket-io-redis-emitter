import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.0.20"
}

group = "de.smartsquare"
version = "1.0.0-SNAPSHOT"
description = "Socket.io emitter example"

repositories {
    mavenCentral()
}

dependencies {
    implementation("de.smartsquare:socket-io-redis-emitter:0.14.2")
    implementation("redis.clients:jedis:5.1.5")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

application {
    mainClass = "de.smartsquare.ApplicationKt"
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "de.smartsquare.ApplicationKt")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
