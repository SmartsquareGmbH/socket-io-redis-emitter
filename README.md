# :construction: Kotlin Socket-Redis Adapter

This library allows you to easily publish notifications via `socket.io` from your kotlin backend. This is currently
under construction and not production ready yet.

It must be used in conjunction with socket.io-redis. The current version is only tested with socket.io-redis@6 and
socket.io@4.

The emitter is also available in other programming languages:

- Java: https://github.com/sunsus/socket.io-java-emitter
- Python: https://pypi.org/project/socket.io-emitter/
- PHP: https://github.com/rase-/socket.io-php-emitter
- Golang: https://github.com/yosuke-furukawa/socket.io-go-emitter
- Perl: https://metacpan.org/pod/SocketIO::Emitter
- Rust: https://github.com/epli2/socketio-rust-emitter

## :bookmark_tabs: Table of content

- [Architecture](#architecture)
- [Getting Started](#getting-started)
    * [Gradle Import](#gradle-import)
    * [Emit Cheatsheet](#emit-cheatsheet)
    * [Example](#example)
- [Limitations](#limitations)

## :green_book: Architecture

![](docs/architecture.png)

## :running: Getting Started

### Gradle Import

The library is only accessible via jitpack until production. You can pick every commit you want using the tag.

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "de.smartsquare.socket-io-redis-emitter:0.9.0"
}
```

### Emit Cheatsheet

```kotlin
val emitter = Emitter(JedisPool("localhost"), namespace = "/")

// Publishing a simple text message
emitter.broadcast(Message.TextMessage(topic = "something", value = "Hello World!"))

// Publishing a complex object is only supported as a map for now.
val payload = mapOf("name" to "deen", "online" to true, "age" to 23)
emitter.broadcast(Message.MapMessage(topic = "something", value = payload))
```

### Example

The [example](example) directory contains a working docker-compose setup which can be started
using `docker-compose --compatibility up`. The setup contains one redis instance, one java publisher, three
socket.io-servers and three consuming socket.io-clients.

## :warning: Limitations

- Publishing types other than primitives or maps is not supported yet.
- The room and namespaces have not been tested yet.
