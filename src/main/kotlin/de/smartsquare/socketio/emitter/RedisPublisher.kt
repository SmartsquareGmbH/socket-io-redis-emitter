package de.smartsquare.socketio.emitter

interface RedisPublisher {
    fun publish(channel: String, message: ByteArray)
}
