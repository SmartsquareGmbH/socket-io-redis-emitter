package de.smartsquare.socketio.emitter

import com.redis.testcontainers.RedisContainer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

        val countDownLatch = CountDownLatch(1)

        val messageListener = { message: Message, _: ByteArray? ->
            message.channel.decodeToString() shouldBeEqualTo "socket.io#/#"
            message.body.decodeToString() shouldContain "test 123"

            countDownLatch.countDown()
        }

        template.requiredConnectionFactory.connection.also {
            it.subscribe(messageListener, "socket.io#/#".toByteArray())
        }

        publisher.broadcast("topic", "test 123")

        countDownLatch.await(5, TimeUnit.SECONDS).shouldBeTrue()
    }
}
