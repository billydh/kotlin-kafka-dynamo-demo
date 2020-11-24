package io.codebrews.kotlinkafkadynamodemo.kafka

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import io.codebrews.createuserrequest.CreateUserRequest
import io.codebrews.kotlinkafkadynamodemo.config.KafkaConfigProperties
import io.codebrews.kotlinkafkadynamodemo.service.CreateUserRequestHandler
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderResult
import reactor.test.StepVerifier
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
@SpringBootTest
internal class KafkaListenerTest {
    @MockBean
    private lateinit var mockCreateUserRequestHandler: CreateUserRequestHandler

    @Autowired
    private lateinit var kafkaConfigProperties: KafkaConfigProperties

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var producer: ReactiveKafkaProducerTemplate<String, CreateUserRequest>

    @BeforeAll
    fun setup() {
        val producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker.brokersAsString)
            .apply {
                this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = kafkaConfigProperties.serializer
                this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = kafkaConfigProperties.serializer
                this[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = ""
            }

        producer = ReactiveKafkaProducerTemplate(SenderOptions.create(producerProps))
    }

    @Test
    fun `should consume CreateUserRequest message and calls the create user request handler method`() {
        val createUserRequest = CreateUserRequest("email@some.com", "Joe", "Jones")

        val producerRecord = ProducerRecord(
            kafkaConfigProperties.createUserRequestTopic,
            UUID.randomUUID().toString(),
            createUserRequest
        )

        StepVerifier.create(producer.send(producerRecord))
            .expectNextMatches { it is SenderResult }
            .verifyComplete()

        given(mockCreateUserRequestHandler.handleCreateUserRequest(createUserRequest)).willReturn(Mono.just(Unit))

        verify(mockCreateUserRequestHandler, timeout(5000L).times(1))
            .handleCreateUserRequest(createUserRequest)
    }
}
