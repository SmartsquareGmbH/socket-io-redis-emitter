package de.smartsquare.socketio.emitter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.msgpack.core.MessagePack
import redis.clients.jedis.Jedis

internal class EmitterTests {

    private val topicSlot = slot<ByteArray>()
    private val pubSlot = slot<ByteArray>()

    private val jedis = mockk<Jedis> {
        every { publish(capture(topicSlot), capture(pubSlot)) } answers { 1 }
    }


    @Test
    internal fun `publish string message`() {
        val publisher = Emitter(jedis)

        publisher.broadcast(Message.TextMessage("some very long message message message message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["emitter",{"type":2,"data":"some very long message message message message","nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish empty message`() {
        val publisher = Emitter(jedis)

        publisher.broadcast(Message.TextMessage(""))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":"","nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message in namespace`() {
        val publisher = Emitter(jedis, namespace = "mynamespace")

        publisher.broadcast(Message.TextMessage("some message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":"some message","nsp":"mynamespace"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message to a room`() {
        val publisher = Emitter(jedis)

        publisher.broadcast(Message.TextMessage("some message"), rooms = listOf("myroom"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":"some message","nsp":"/"},{"rooms":["myroom"],"except":[],"flags":{}}]"""

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldEqual "socket.io#/#myroom#"
    }

    @Test
    internal fun `publish message to two rooms exclusively`() {
        val publisher = Emitter(jedis)

        publisher.broadcast(Message.TextMessage("some message"), rooms = listOf("a", "b"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":"some message","nsp":"/"},{"rooms":["a","b"],"except":[],"flags":{}}]"""

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldEqual "socket.io#/#"
    }

    @Test
    internal fun `publish message to all rooms except one`() {
        val publisher = Emitter(jedis)

        publisher.broadcast(Message.TextMessage("some message"), except = listOf("a"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()
        encoded shouldEqual """["emitter",{"type":2,"data":"some message","nsp":"/"},{"rooms":[],"except":["a"],"flags":{}}]"""
    }
}
