package io.codebrews.kotlinkafkadynamodemo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "application.kafka")
data class KafkaConfigProperties(
    val broker: String,
    val serializer: String,
    val deserializer: String,
    val schemaRegistryUrl: String,
    val createUserRequestTopic: String,
    val userCreatedEventTopic: String
)
