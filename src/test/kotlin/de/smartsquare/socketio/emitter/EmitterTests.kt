package de.smartsquare.socketio.emitter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.msgpack.core.MessagePack
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

internal class EmitterTests {

    private val topicSlot = slot<ByteArray>()
    private val pubSlot = slot<ByteArray>()

    private val jedis = mockk<Jedis>(relaxed = true) {
        every { publish(capture(topicSlot), capture(pubSlot)) } answers { 1 }
    }

    private val jedisPool = mockk<JedisPool> {
        every { resource } returns jedis
    }

    @Test
    internal fun `publish string message`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", "some very long message message message message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["emitter",{"type":2,"data":["topic","some very long message message message message"],"nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `emitter releases the resource`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", "some very long message message message message"))

        verify(exactly = 1) { jedis.close() }
    }

    @Test
    internal fun `customize emitter id`() {
        val publisher = Emitter(jedisPool, "backend-1")

        publisher.broadcast(Message.TextMessage("topic", "some very long message message message message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["backend-1",{"type":2,"data":["topic","some very long message message message message"],"nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish empty message`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", ""))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic",""],"nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message in namespace`() {
        val publisher = Emitter(jedisPool, namespace = "mynamespace")

        publisher.broadcast(Message.TextMessage("topic", "some message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic","some message"],"nsp":"mynamespace"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message to a room`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", "some message"), rooms = listOf("myroom"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic","some message"],"nsp":"/"},{"rooms":["myroom"],"except":[],"flags":{}}]"""

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldEqual "socket.io#/#myroom#"
    }

    @Test
    internal fun `publish message to two rooms exclusively`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", "some message"), rooms = listOf("a", "b"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic","some message"],"nsp":"/"},{"rooms":["a","b"],"except":[],"flags":{}}]"""

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldEqual "socket.io#/#"
    }

    @Test
    internal fun `publish message to all rooms except one`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.TextMessage("topic", "some message"), except = listOf("a"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic","some message"],"nsp":"/"},{"rooms":[],"except":["a"],"flags":{}}]"""
    }

    @Test
    internal fun `publish json message including only primitives`() {
        val publisher = Emitter(jedisPool)

        publisher.broadcast(Message.MapMessage("topic", mapOf("name" to "deen", "age" to 23, "height" to 1.9)))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":["topic",{"name":"deen","age":23,"height":1.9}],"nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test()
    internal fun `throw exception on unknown type`() {
        val publisher = Emitter(jedisPool)

        val message = Message.MapMessage("topic", mapOf("name" to "deen", "attributes" to PersonAttributes(age = 23)))

        assertThrows<IllegalStateException> { publisher.broadcast(message) }
    }

    data class PersonAttributes(val age: Int)
}
