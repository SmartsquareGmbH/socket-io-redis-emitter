package de.smartsquare.socketio.emitter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.msgpack.core.MessagePack
import redis.clients.jedis.Jedis

internal class EmitterTests {

    private val pubSlot = slot<ByteArray>()

    private val jedis = mockk<Jedis> {
        every { publish(any(), capture(pubSlot)) } answers { 1 }
    }

    private val publisher = Emitter(jedis)

    @Test
    internal fun `publish string message`() {
        publisher.broadcast(Message.TextMessage("some very long message message message message"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["emitter",{"type":2,"data":"some very long message message message message","nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish empty message`() {
        publisher.broadcast(Message.TextMessage(""))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["emitter",{"type":2,"data":"","nsp":"/"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message in namespace`() {
        publisher.broadcast(Message.TextMessage("some message", "mynamespace"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        encoded shouldEqual """["emitter",{"type":2,"data":"some message","nsp":"mynamespace"},{"rooms":[],"except":[],"flags":{}}]"""
    }

    @Test
    internal fun `publish message with flags`() {
    }

    @Test
    internal fun `publish message to multiple rooms`() {
    }

    @Test
    internal fun `publish message to all rooms except one`() {
    }
}
