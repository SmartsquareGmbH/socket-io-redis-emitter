package de.smartsquare.socketio.emitter

interface RedisPublisher {
    fun publish(channel: ByteArray, message: ByteArray)
}
