package de.smartsquare.socketio.emitter

import redis.clients.jedis.JedisPool

class JedisPublisher(private val jedisPool: JedisPool) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        jedisPool.resource.use { it.publish(channel.toByteArray(), message) }
    }
}
