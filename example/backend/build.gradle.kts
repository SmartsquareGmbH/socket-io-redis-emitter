plugins {
    application
    kotlin("jvm") version "2.3.20"
}

group = "de.smartsquare"
version = "1.0.0-SNAPSHOT"
description = "Socket.io emitter example"

repositories {
    mavenCentral()
}

dependencies {
    implementation("de.smartsquare:socket-io-redis-emitter:0.15.0")
    implementation("redis.clients:jedis:7.4.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "de.smartsquare.ApplicationKt"
}
