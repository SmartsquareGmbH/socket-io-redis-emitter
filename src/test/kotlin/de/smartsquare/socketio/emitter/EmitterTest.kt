package de.smartsquare.socketio.emitter

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.msgpack.core.MessagePack
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

class EmitterTest {

    private val topicSlot = slot<ByteArray>()
    private val pubSlot = slot<ByteArray>()

    private val publisher = mockk<RedisPublisher>(relaxed = true) {
        every { publish(capture(topicSlot), capture(pubSlot)) } answers {}
    }

    @Test
    fun `publish string message`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", "some very long message message message message")

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some very long message message message message"
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `customize emitter id`() {
        val publisher = Emitter(publisher, "backend-1")

        publisher.broadcast("topic", "some very long message message message message")

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "backend-1",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some very long message message message message"
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `publish empty message`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", "")

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  ""
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `publish message in namespace`() {
        val publisher = Emitter(publisher, namespace = "mynamespace")

        publisher.broadcast("topic", "some message")

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some message"
                ],
                "nsp": "mynamespace"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `publish message to a room`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", "some message", rooms = listOf("myroom"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some message"
                ],
                "nsp": "/"
              },
              {
                "rooms": [
                  "myroom"
                ],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldBeEqualTo "socket.io#/#myroom#"
    }

    @Test
    fun `publish message to two rooms exclusively`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", "some message", rooms = listOf("a", "b"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some message"
                ],
                "nsp": "/"
              },
              {
                "rooms": [
                  "a",
                  "b"
                ],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()

        val topic = topicSlot.captured.toString(charset = Charsets.UTF_8)
        topic shouldBeEqualTo "socket.io#/#"
    }

    @Test
    fun `publish message to all rooms except one`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", "some message", except = listOf("a"))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  "some message"
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [
                  "a"
                ],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `publish json message including only primitives`() {
        val publisher = Emitter(publisher)

        publisher.broadcast("topic", mapOf("name" to "deen", "age" to 23, "height" to 1.9))

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  {
                    "name": "deen",
                    "age": 23,
                    "height": 1.9
                  }
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }

    @Test
    fun `publish json message with date times`() {
        val publisher = Emitter(publisher)

        val date = Date(1609462861001)
        val localDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1, 1)
        val offsetDateTime = OffsetDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)
        val zonedDateTime = ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of("Etc/UTC"))

        publisher.broadcast(
            "topic",
            mapOf(
                "date" to date,
                "localDateTime" to localDateTime,
                "offsetDateTime" to offsetDateTime,
                "zonedDateTime" to zonedDateTime,
            ),
        )

        val encoded = MessagePack.newDefaultUnpacker(pubSlot.captured).unpackValue().toString()

        // language=json
        encoded shouldBeEqualToJson """
            [
              "emitter",
              {
                "type": 2,
                "data": [
                  "topic",
                  {
                    "date": "2021-01-01T01:01:01.001+00:00",
                    "localDateTime": "2021-01-01T01:01:01.000000001",
                    "offsetDateTime": "2021-01-01T01:01:01.000000001Z",
                    "zonedDateTime": "2021-01-01T01:01:01.000000001Z"
                  }
                ],
                "nsp": "/"
              },
              {
                "rooms": [],
                "except": [],
                "flags": {}
              }
            ]
        """.trimIndent()
    }
}
