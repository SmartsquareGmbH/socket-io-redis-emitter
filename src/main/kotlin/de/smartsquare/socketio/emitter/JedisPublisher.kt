package de.smartsquare.socketio.emitter

import redis.clients.jedis.Jedis

class JedisPublisher(private val jedis: Jedis) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        jedis.use { it.publish(channel.toByteArray(), message) }
    }
}
