package de.smartsquare.socketio.emitter

sealed class Message {

    data class TextMessage(val topic: String, val value: String) : Message()
    data class MapMessage(val topic: String, val value: Map<String, Any>) : Message()
}
