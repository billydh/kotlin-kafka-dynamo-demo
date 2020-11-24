package io.codebrews.kotlinkafkadynamodemo.service

import io.codebrews.createuserrequest.CreateUserRequest
import io.codebrews.kotlinkafkadynamodemo.CustomerPersist
import io.codebrews.kotlinkafkadynamodemo.repo.CustomerRepo
import io.codebrews.kotlinkafkadynamodemo.kafka.KafkaPublisher
import io.codebrews.usercreatedevent.UserCreatedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Component
class CreateUserRequestHandler(private val customerRepo: CustomerRepo,
                               private val kafkaPublisher: KafkaPublisher) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun handleCreateUserRequest(request: CreateUserRequest): Mono<Unit> {
        val customerId: String = generateCustomerId()
        val customerPersist = CustomerPersist(customerId, request.getEmail(), request.getFirstName(), request.getLastName())

        return customerRepo.saveCustomer(customerPersist)
            .flatMap {
                kafkaPublisher.publishUserCreatedEvent(KafkaPublisher.generateMessageKey(),
                    UserCreatedEvent(customerId, customerPersist.emailAddress, Instant.now().toEpochMilli()))
            }
            .map { }
            .doOnError { logger.error("Exception while trying to create a new user", it) }
    }

    private fun generateCustomerId(): String {
        return UUID.randomUUID().toString()
    }
}
