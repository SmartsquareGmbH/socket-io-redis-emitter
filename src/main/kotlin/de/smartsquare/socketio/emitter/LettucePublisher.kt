package de.smartsquare.socketio.emitter

import io.lettuce.core.api.sync.RedisCommands

class LettucePublisher(private val commands: RedisCommands<String, String>) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        commands.publish(channel, message.decodeToString())
    }
}
