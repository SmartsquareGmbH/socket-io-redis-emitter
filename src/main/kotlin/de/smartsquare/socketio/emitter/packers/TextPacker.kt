package de.smartsquare.socketio.emitter.packers

import de.smartsquare.socketio.emitter.Message
import de.smartsquare.socketio.emitter.Metadata
import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream

internal class TextPacker {

    fun pack(message: Message.TextMessage, metadata: Metadata): ByteArray {
        val packerStream = ByteArrayOutputStream()

        MessagePack.newDefaultPacker(packerStream).use {
            it.packArrayHeader(3)
            it.packString("emitter")
            it.packMapHeader(3)
            it.packString("type")
            it.packInt(2)
            it.packString("data")
            it.packString(message.value)
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
