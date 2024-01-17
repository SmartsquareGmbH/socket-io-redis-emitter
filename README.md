# :construction: Socket.io Redis Emitter

This library allows you to easily publish notifications via `socket.io` from your java/kotlin/jvm backend.

To be used in conjunction with the [redis adapter](https://socket.io/docs/v4/redis-adapter/).

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
    * [Usage](#usage)
    * [Example](#example)

## Architecture

![](docs/architecture.png)

## Getting Started

### Gradle Import

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "de.smartsquare:socket-io-redis-emitter:0.14.0"
}
```

### Usage

This library comes with a default implementation
for [Jedis](https://github.com/redis/jedis), [Lettuce](https://lettuce.io/)
and [Spring Data Redis](https://spring.io/projects/spring-data-redis/).

#### With Lettuce

```kotlin
// Lettuce setup (https://lettuce.io/docs/getting-started.html)
var redisClient = RedisClient.create("redis://localhost:6379")
var connection = redisClient.connect()
var syncCommands = connection.sync()

val emitter = Emitter(LettucePublisher(syncCommands))

// Publishing a simple text message
emitter.broadcast(topic = "something", value = "Hello World!")

// Publishing a complex object is only supported as a map for now.
val payload = mapOf("name" to "deen", "online" to true, "age" to 23)
emitter.broadcast(topic = "something", value = payload)
```

#### With Jedis

```kotlin
// Jedis setup (https://github.com/redis/jedis/wiki/Getting-started#basic-usage-example)
var pool = JedisPool("redis://localhost:6379")

val emitter = Emitter(JedisPublisher(pool))

emitter.broadcast(topic = "something", value = "Hello World!")
```

#### With Spring Data Redis

```kotlin
@Configuration
class RedisConfig {
    @Bean
    fun emitter(redisTemplate: StringRedisTemplate): Emitter {
        return Emitter(SpringDataPublisher(redisTemplate))
    }
}

@Service
class TestService(private val emitter: Emitter) {
    fun emit() {
      emitter.broadcast(topic = "something", value = "Hello World!")
    }
}
```

#### Extending

Implement the `RedisPublisher` interface to add support for another client.

```kotlin
class OtherClientPublisher(private val client: OtherClient) : RedisPublisher {
    override fun publish(channel: String, message: ByteArray) {
        client.publish(channel, message.decodeToString())
    }
}
```

### Serialization with Jackson

The `Emitter` uses Jackson internally to convert payloads. It is by default configured to produce valid messages, but
can be overridden:

```kotlin
// This is the default used in the Emitter. Adjust to your needs.
val myObjectMapper = ObjectMapper(MessagePackFactory())
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) /* Does not work without for date times 
                                                                with high precision. */

// Construct as before but additionally with the ObjectMapper above.
val emitterWithCustomObjectMapper = Emitter(JedisPool("localhost"), namespace = "/", objectMapper = myObjectMapper)

emitterWithCustomObjectMapper.broadcast(topic = "something", value = "Hello World!")
```

### Example

The [example](example) directory contains a working docker-compose setup which can be started
using `docker-compose --compatibility up`. The setup contains one redis instance, one java publisher, three
socket.io-servers and three consuming socket.io-clients.
