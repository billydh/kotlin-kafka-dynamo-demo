package io.codebrews.kotlinkafkadynamodemo.kafka

import io.codebrews.createuserrequest.CreateUserRequest
import io.codebrews.kotlinkafkadynamodemo.config.KafkaConfigProperties
import io.codebrews.usercreatedevent.UserCreatedEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import java.util.*

@Component
class KafkaPublisher(private val kafkaConfigProperties: KafkaConfigProperties) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val producerProps: Map<String, String> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfigProperties.broker,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to kafkaConfigProperties.serializer,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to kafkaConfigProperties.serializer,
        "schema.registry.url" to kafkaConfigProperties.schemaRegistryUrl
    )

    private val createUserRequestSenderOptions: SenderOptions<String, CreateUserRequest> = SenderOptions.create<String, CreateUserRequest>(producerProps)
    private val createUserRequestKafkaSender: KafkaSender<String, CreateUserRequest> = KafkaSender.create(createUserRequestSenderOptions)

    private val userCreatedEventSenderOptions: SenderOptions<String, UserCreatedEvent> = SenderOptions.create<String, UserCreatedEvent>(producerProps)
    private val userCreatedEventKafkaSender: KafkaSender<String, UserCreatedEvent> = KafkaSender.create(userCreatedEventSenderOptions)

    fun publishMessage(key: String, value: CreateUserRequest): Mono<Void> {
        val producerRecord: ProducerRecord<String, CreateUserRequest> = ProducerRecord(kafkaConfigProperties.createUserRequestTopic, key, value)

        return createUserRequestKafkaSender.createOutbound()
            .send(Mono.just(producerRecord))
            .then()
            .doOnSuccess { logger.info("Successfully sent a CreateUserRequest message with id $key") }
    }

    fun publishUserCreatedEvent(key: String, value: UserCreatedEvent): Mono<Void> {
        val producerRecord: ProducerRecord<String, UserCreatedEvent> = ProducerRecord(kafkaConfigProperties.userCreatedEventTopic, key, value)

        return userCreatedEventKafkaSender.createOutbound()
            .send(Mono.just(producerRecord))
            .then()
            .doOnSuccess { logger.info("Successfully sent a UserCreatedEvent message with id $key") }
    }

    companion object {
        fun generateMessageKey(): String {
            return UUID.randomUUID().toString()
        }
    }
}
