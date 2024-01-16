package de.smartsquare.socketio.emitter

import com.redis.testcontainers.RedisContainer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SpringDataPublisherTest {

    @Container
    private val redis = RedisContainer("redis:6-alpine")

    private lateinit var lettuceConnectionFactory: LettuceConnectionFactory
    private lateinit var template: StringRedisTemplate

    @BeforeEach
    fun setUp() {
        lettuceConnectionFactory = LettuceConnectionFactory(redis.host, redis.firstMappedPort).apply {
            afterPropertiesSet()
        }

        template = StringRedisTemplate().apply {
            connectionFactory = lettuceConnectionFactory
            afterPropertiesSet()
        }
    }

    @AfterEach
    fun tearDown() {
        lettuceConnectionFactory.destroy()
    }

    @Test
    fun `publish string message`() {
        val publisher = Emitter(SpringDataPublisher(template))

        val (channel, message) = awaitRedisMessage(redis.redisURI, "socket.io#/#") {
            publisher.broadcast("topic", "test 123")
        }

        channel shouldBeEqualTo "socket.io#/#"
        message shouldContain "test 123"
    }
}
