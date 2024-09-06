package de.smartsquare

import de.smartsquare.socketio.emitter.Emitter
import de.smartsquare.socketio.emitter.JedisPublisher
import redis.clients.jedis.JedisPool
import kotlin.concurrent.fixedRateTimer

fun main() {
    val emitter = Emitter(JedisPublisher(JedisPool("redis", 6379)))

    fixedRateTimer("notifications", false, 0L, 1000L) {
        println("Publishing a new notification...")

        // Publishing a simple text message
        emitter.broadcast(topic = "something", value = "Hello World!")

        // Publishing a complex object.
        val payload = mapOf("name" to "deen", "online" to true, "age" to 23)
        emitter.broadcast(topic = "something", value = payload)
    }
}
