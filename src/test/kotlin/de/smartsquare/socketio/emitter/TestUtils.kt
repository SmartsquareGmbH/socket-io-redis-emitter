package de.smartsquare.socketio.emitter

import org.skyscreamer.jsonassert.JSONAssert

infix fun String.shouldBeEqualToJson(expected: String): String = apply { JSONAssert.assertEquals(this, expected, true) }
