package de.smartsquare.socketio.emitter

import com.fasterxml.jackson.databind.ObjectMapper

internal class MessageConverter(private val objectMapper: ObjectMapper) {

    fun convert(message: SocketIoMessage): ByteArray {
        val convertedMessage = arrayOf(
            message.id,
            mapOf(
                "type" to 2,
                "data" to arrayOf(
                    message.topic,
                    message.value
                ),
                "nsp" to message.namespace
            ),
            mapOf(
                "rooms" to message.rooms,
                "except" to message.except,
                "flags" to emptyMap<String, String>()
            )
        )

        return objectMapper.writeValueAsBytes(convertedMessage)
    }
}
