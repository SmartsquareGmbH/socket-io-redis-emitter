package de.smartsquare.socketio.emitter

import com.redis.testcontainers.RedisContainer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

        val countDownLatch = CountDownLatch(1)

        val listener = object : JedisPubSub() {
            override fun onMessage(channel: String, message: String) {
                channel shouldBeEqualTo "socket.io#/#"
                message shouldContain "test 123"

                countDownLatch.countDown()
            }
        }

        val executor = Executors.newSingleThreadExecutor()
        val jedis = pool.resource

        try {
            executor.submit { jedis.subscribe(listener, "socket.io#/#") }

            publisher.broadcast("topic", "test 123")

            countDownLatch.await(5, TimeUnit.SECONDS).shouldBeTrue()
        } finally {
            listener.unsubscribe()
            executor.shutdown()
        }
    }
}
