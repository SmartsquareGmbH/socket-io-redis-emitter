package de.smartsquare.socketio.emitter

import de.smartsquare.socketio.emitter.packers.MapPacker
import de.smartsquare.socketio.emitter.packers.TextPacker
import redis.clients.jedis.JedisPool

class Emitter @JvmOverloads constructor(private val jedis: JedisPool, private val id: String = "emitter", private val namespace: String = "/") {

    private val textPacker = TextPacker()
    private val jsonPacker = MapPacker()

    /**
     *
     */
    @JvmOverloads
    fun broadcast(message: Message, rooms: List<String> = emptyList(), except: List<String> = emptyList()) {
        val metadata = Metadata(id, namespace, rooms, except)

        val payload = when (message) {
            is Message.MapMessage -> jsonPacker.pack(message, metadata)
            is Message.TextMessage -> textPacker.pack(message, metadata)
        }

        if (rooms.size == 1) {
            jedis.resource.use { it.publish("socket.io#${namespace}#${rooms.first()}#".toByteArray(), payload) }
        } else {
            jedis.resource.use { it.publish("socket.io#${namespace}#".toByteArray(), payload) }
        }
    }
}
