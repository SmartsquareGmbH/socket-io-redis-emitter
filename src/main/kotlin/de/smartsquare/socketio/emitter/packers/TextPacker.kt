package de.smartsquare.socketio.emitter.packers

import de.smartsquare.socketio.emitter.Message
import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream

class TextPacker {

    fun pack(message: Message.TextMessage): ByteArray {
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
            it.packString(message.namespace)
            it.packMapHeader(3)
            it.packString("rooms")
            it.packArrayHeader(0)
            it.packString("except")
            it.packArrayHeader(0)
            it.packString("flags")
            it.packMapHeader(0)
        }

        return packerStream.toByteArray()
    }
}
