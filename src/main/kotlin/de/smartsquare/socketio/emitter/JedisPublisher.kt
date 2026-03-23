package de.smartsquare.socketio.emitter

import redis.clients.jedis.RedisClient

class JedisPublisher(private val redisClient: RedisClient) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        redisClient.publish(channel.toByteArray(), message)
    }
}
