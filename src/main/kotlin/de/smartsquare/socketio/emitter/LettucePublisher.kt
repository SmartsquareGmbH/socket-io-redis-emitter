package de.smartsquare.socketio.emitter

import io.lettuce.core.api.StatefulRedisConnection

class LettucePublisher(private val connection: StatefulRedisConnection<Any, Any>) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        connection.use { it.sync().publish(channel, message.decodeToString()) }
    }
}
