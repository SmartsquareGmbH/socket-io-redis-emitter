package de.smartsquare.socketio.emitter

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.amshove.kluent.shouldBeTrue
import org.skyscreamer.jsonassert.JSONAssert
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

infix fun String.shouldBeEqualToJson(expected: String): String = apply { JSONAssert.assertEquals(expected, this, true) }

/**
 * Returns the first message published on the given [channel] of the redis available under the [redisURI]. Waits for at
 * most five seconds after executing [body] for the message to arrive.
 */
fun awaitRedisMessage(redisURI: String, channel: String, body: () -> Unit): RedisMessage {
    return RedisClient.create(redisURI).use { client ->
        var result: RedisMessage? = null

        val countDownLatch = CountDownLatch(1)

        val listener = object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                result = RedisMessage(channel, message)

                countDownLatch.countDown()
            }
        }

        client.connectPubSub().apply {
            addListener(listener)
            sync().subscribe(channel)
        }

        body()

        countDownLatch.await(5, TimeUnit.SECONDS).shouldBeTrue()

        result!!
    }
}

data class RedisMessage(val channel: String, val message: String)
