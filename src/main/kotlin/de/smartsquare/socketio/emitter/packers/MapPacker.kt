package de.smartsquare.socketio.emitter.packers

import de.smartsquare.socketio.emitter.Message
import de.smartsquare.socketio.emitter.Metadata
import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.temporal.Temporal
import java.util.Date
import java.util.TimeZone

internal class MapPacker {

    private companion object {
        @JvmStatic
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun pack(message: Message.MapMessage, metadata: Metadata): ByteArray {
        val packerStream = ByteArrayOutputStream()

        MessagePack.newDefaultPacker(packerStream).use {
            it.packArrayHeader(3)
            it.packString(metadata.id)
            it.packMapHeader(3)
            it.packString("type")
            it.packInt(2)
            it.packString("data")

            it.packArrayHeader(2)
            it.packString(message.topic)

            it.packMapHeader(message.value.size)
            for ((key, value) in message.value) {
                it.packString(key)

                when (value) {
                    is String -> it.packString(value)
                    is Int -> it.packInt(value)
                    is Double -> it.packDouble(value)
                    is Long -> it.packLong(value)
                    is Boolean -> it.packBoolean(value)
                    is Date -> it.packString(dateFormat.format(value))
                    is Temporal -> it.packString(value.toString())
                    else -> error("The type of $key is not implemented yet. Feel free to open a pull request.")
                }
            }

            it.packString("nsp")
            it.packString(metadata.namespace)
            it.packMapHeader(3)

            it.packString("rooms")
            it.packArrayHeader(metadata.rooms.size)
            for (room in metadata.rooms) {
                it.packString(room)
            }

            it.packString("except")
            it.packArrayHeader(metadata.except.size)
            for (room in metadata.except) {
                it.packString(room)
            }

            it.packString("flags")
            it.packMapHeader(0)
        }

        return packerStream.toByteArray()
    }
}
