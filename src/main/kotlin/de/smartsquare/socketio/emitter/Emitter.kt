package de.smartsquare.socketio.emitter

import de.smartsquare.socketio.emitter.packers.BinaryPacker
import de.smartsquare.socketio.emitter.packers.JsonPacker
import de.smartsquare.socketio.emitter.packers.TextPacker
import redis.clients.jedis.Jedis

class Emitter(private val jedis: Jedis, private val namespace: String = "/") {

    private val textPacker = TextPacker()
    private val jsonPacker = JsonPacker()
    private val binaryPacker = BinaryPacker()

    fun broadcast(message: Message) {
        val payload = when (message) {
            is Message.BinaryMessage -> binaryPacker.pack(message)
            is Message.JSONMessage -> jsonPacker.pack(message)
            is Message.TextMessage -> textPacker.pack(message)
        }

        jedis.publish("socket.io#${namespace}#".toByteArray(), payload)
    }
}
