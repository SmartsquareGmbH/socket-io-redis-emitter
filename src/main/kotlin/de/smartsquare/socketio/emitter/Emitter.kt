package de.smartsquare.socketio.emitter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.msgpack.jackson.dataformat.MessagePackFactory
import redis.clients.jedis.JedisPool

class Emitter @JvmOverloads constructor(
    private val jedis: JedisPool,
    private val id: String = "emitter",
    private val namespace: String = "/",
    objectMapper: ObjectMapper = ObjectMapper(MessagePackFactory())
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // This does not work because of too large numbers.
) {

    private val messageConverter = MessageConverter(objectMapper)

    @JvmOverloads
    fun broadcast(topic: String, value: Any, rooms: List<String> = emptyList(), except: List<String> = emptyList()) {
        val payload = messageConverter.convert(SocketIoMessage(id, topic, value, namespace, rooms, except))

        if (rooms.size == 1) {
            jedis.resource.use { it.publish("socket.io#$namespace#${rooms.first()}#".toByteArray(), payload) }
        } else {
            jedis.resource.use { it.publish("socket.io#$namespace#".toByteArray(), payload) }
        }
    }
}
