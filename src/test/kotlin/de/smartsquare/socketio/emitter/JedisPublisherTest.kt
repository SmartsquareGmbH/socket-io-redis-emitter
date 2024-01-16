package de.smartsquare.socketio.emitter

import com.redis.testcontainers.RedisContainer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import redis.clients.jedis.JedisPool

@Testcontainers
class JedisPublisherTest {

    @Container
    private val redis = RedisContainer("redis:6-alpine")

    private lateinit var pool: JedisPool

    @BeforeEach
    fun setUp() {
        pool = JedisPool(redis.redisURI)
    }

    @AfterEach
    fun tearDown() {
        pool.close()
    }

    @Test
    fun `publish string message`() {
        val publisher = Emitter(JedisPublisher(pool))

        val (channel, message) = awaitRedisMessage(redis.redisURI, "socket.io#/#") {
            publisher.broadcast("topic", "test 123")
        }

        channel shouldBeEqualTo "socket.io#/#"
        message shouldContain "test 123"
    }
}
