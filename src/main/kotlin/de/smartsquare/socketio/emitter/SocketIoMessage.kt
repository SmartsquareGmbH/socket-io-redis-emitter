package de.smartsquare.socketio.emitter

internal data class SocketIoMessage(
    val id: String,
    val topic: String,
    val value: Any,
    val namespace: String,
    val rooms: List<String>,
    val except: List<String>
)
