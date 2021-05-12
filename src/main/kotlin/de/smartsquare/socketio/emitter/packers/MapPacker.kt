package de.smartsquare.socketio.emitter.packers

import de.smartsquare.socketio.emitter.Message
import de.smartsquare.socketio.emitter.Metadata
import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream

internal class MapPacker {

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
