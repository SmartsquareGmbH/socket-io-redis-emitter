package de.smartsquare.socketio.emitter

import de.smartsquare.socketio.emitter.packers.MapPacker
import de.smartsquare.socketio.emitter.packers.TextPacker
import redis.clients.jedis.Jedis

class Emitter(private val jedis: Jedis, private val id: String = "emitter", private val namespace: String = "/") {

    private val textPacker = TextPacker()
    private val jsonPacker = MapPacker()

    /**
     *
     */
    fun broadcast(message: Message, rooms: List<String> = emptyList(), except: List<String> = emptyList()) {
        val metadata = Metadata(id, namespace, rooms, except)

        val payload = when (message) {
            is Message.MapMessage -> jsonPacker.pack(message, metadata)
            is Message.TextMessage -> textPacker.pack(message, metadata)
        }

        if (rooms.size == 1) {
            jedis.publish("socket.io#${namespace}#${rooms.first()}#".toByteArray(), payload)
        } else {
            jedis.publish("socket.io#${namespace}#".toByteArray(), payload)
        }
    }
}
