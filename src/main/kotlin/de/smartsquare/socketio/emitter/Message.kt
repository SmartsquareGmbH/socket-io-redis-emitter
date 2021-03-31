package de.smartsquare.socketio.emitter

sealed class Message(open val namespace: String) {

    data class TextMessage(val value: String, override val namespace: String = "/") : Message(namespace)
    data class JSONMessage(val value: String, override val namespace: String = "/") : Message(namespace)
    data class BinaryMessage(val value: ByteArray, override val namespace: String = "/") : Message(namespace)
}
