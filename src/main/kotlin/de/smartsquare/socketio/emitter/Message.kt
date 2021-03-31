package de.smartsquare.socketio.emitter

sealed class Message {

    data class TextMessage(val value: String) : Message()
    data class MapMessage(val value: Map<String, Any>) : Message()
}
