package de.smartsquare.socketio.emitter

import com.redis.testcontainers.RedisContainer
import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Testcontainers
class LettucePublisherTest {

    @Container
    private val redis = RedisContainer("redis:6-alpine")

    private lateinit var client: RedisClient

    @BeforeEach
    fun setUp() {
        client = RedisClient.create(redis.redisURI)
    }

    @AfterEach
    fun tearDown() {
        client.close()
    }

    @Test
    fun `publish string message`() {
        val connection = client.connect().sync()
        val publisher = Emitter(LettucePublisher(connection))

        val countDownLatch = CountDownLatch(1)

        val listener = object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                channel shouldBeEqualTo "socket.io#/#"
                message shouldContain "test 123"

                countDownLatch.countDown()
            }
        }

        client.connectPubSub().apply {
            addListener(listener)
            sync().subscribe("socket.io#/#")
        }

        publisher.broadcast("topic", "test 123")

        countDownLatch.await(5, TimeUnit.SECONDS).shouldBeTrue()
    }
}
