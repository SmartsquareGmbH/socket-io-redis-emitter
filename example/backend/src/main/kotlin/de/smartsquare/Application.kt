package de.smartsquare

import de.smartsquare.socketio.emitter.Emitter
import de.smartsquare.socketio.emitter.Message
import redis.clients.jedis.JedisPool
import kotlin.concurrent.fixedRateTimer

fun main() {
    val emitter = Emitter(JedisPool("redis"))

    fixedRateTimer("notifications", false, 0L, 1000L) {
        println("Publishing a new notification...")

        // Publishing a simple text message
        emitter.broadcast(Message.TextMessage(topic = "something", value = "Hello World!"))

        // Publishing a complex object is only supported as a map for now.
        val payload = mapOf("name" to "deen", "online" to true, "age" to 23)
        emitter.broadcast(Message.MapMessage(topic = "something", value = payload))
    }
}
