package de.smartsquare.socketio.emitter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.msgpack.jackson.dataformat.MessagePackFactory

class Emitter @JvmOverloads constructor(
    private val redisPublisher: RedisPublisher,
    private val id: String = "emitter",
    private val namespace: String = "/",
    objectMapper: ObjectMapper = ObjectMapper(MessagePackFactory())
        .findAndRegisterModules()
        // Does not work without for date times with high precision.
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
) {

    private val messageConverter = MessageConverter(objectMapper)

    @JvmOverloads
    fun broadcast(topic: String, value: Any, rooms: List<String> = emptyList(), except: List<String> = emptyList()) {
        val payload = messageConverter.convert(SocketIoMessage(id, topic, value, namespace, rooms, except))

        if (rooms.size == 1) {
            redisPublisher.publish("socket.io#$namespace#${rooms.first()}#", payload)
        } else {
            redisPublisher.publish("socket.io#$namespace#", payload)
        }
    }
}
