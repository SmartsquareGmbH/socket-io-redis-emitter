package de.smartsquare.socketio.emitter

sealed class Message() {

    data class TextMessage(val value: String) : Message()
    data class JSONMessage(val value: String) : Message()
    data class BinaryMessage(val value: ByteArray) : Message()
}
