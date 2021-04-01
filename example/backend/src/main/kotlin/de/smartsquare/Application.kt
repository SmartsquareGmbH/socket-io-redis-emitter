package de.smartsquare

import de.smartsquare.socketio.emitter.Emitter
import de.smartsquare.socketio.emitter.Message
import redis.clients.jedis.Jedis
import kotlin.concurrent.fixedRateTimer

fun main() {
    val emitter = Emitter(Jedis("localhost"))

    fixedRateTimer("notifications", false, 0L, 1000L) {
        println("Publishing a new notification...")

        // Objects are currently published as map
        val payload = mapOf("name" to "deen", "online" to true, "age" to 23)
        emitter.broadcast(Message.MapMessage(topic = "something", value = payload))
    }
}
