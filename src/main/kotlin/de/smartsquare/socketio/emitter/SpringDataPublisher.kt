package de.smartsquare.socketio.emitter

import org.springframework.data.redis.core.RedisTemplate

class SpringDataPublisher(private val redisTemplate: RedisTemplate<String, String>) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        redisTemplate.execute({ it.publish(channel.toByteArray(), message) }, true)
    }
}
