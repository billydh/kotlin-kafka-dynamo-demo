package io.codebrews.kotlinkafkadynamodemo.kafka

import io.codebrews.createuserrequest.CreateUserRequest
import io.codebrews.kotlinkafkadynamodemo.config.KafkaConfigProperties
import io.codebrews.kotlinkafkadynamodemo.service.CreateUserRequestHandler
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.time.Duration
import java.util.*

@Component
class KafkaListener(kafkaConfigProperties: KafkaConfigProperties,
                    private val createUserRequestHandler: CreateUserRequestHandler) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val consumerProps: Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfigProperties.broker,
        ConsumerConfig.GROUP_ID_CONFIG to "create-user-request-v1",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to kafkaConfigProperties.deserializer,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to kafkaConfigProperties.deserializer,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        "schema.registry.url" to kafkaConfigProperties.schemaRegistryUrl,
        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true
    )

    private val receiverOptions: ReceiverOptions<String, CreateUserRequest> = ReceiverOptions
        .create<String, CreateUserRequest>(consumerProps)
        .commitInterval(Duration.ZERO)
        .commitBatchSize(0)
        .subscription(Collections.singleton(kafkaConfigProperties.createUserRequestTopic))

    @Bean
    private fun listen(): Disposable {
        return KafkaReceiver.create(receiverOptions)
            .receive()
            .concatMap {record ->
                createUserRequestHandler.handleCreateUserRequest(record.value())
                    .then(record.receiverOffset().commit())
                    .doOnError { logger.error("Exception while trying to consume and commit a CreateUserRequest message", it) }
            }
            .subscribe()
    }
}
