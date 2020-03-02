package io.codebrews.kotlinkafkadynamodemo

import io.codebrews.createuserrequest.CreateUserRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.json
import reactor.core.publisher.Mono

@Component
class CustomerHandler(private val customerRepo: CustomerRepo,
                      private val kafkaPublisher: KafkaPublisher) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun saveCustomerInformation(request: ServerRequest): Mono<ServerResponse> {

        return request.bodyToMono(Customer::class.java)
            .flatMap {
                Mono.just(KafkaPublisher.generateMessageKey())
                    .zipWith(Mono.just(CreateUserRequest(it.emailAddress, it.firstName, it.lastName)))
            }
            .flatMap { kafkaPublisher.publishMessage(it.t1, it.t2) }
            .flatMap { ServerResponse.ok().build() }
            .doOnError { logger.error("Exception while trying to process a request", it) }
    }

    fun retrieveCustomerInformation(request: ServerRequest): Mono<ServerResponse> {

        return Mono.fromSupplier { request.pathVariable("customerId") }
            .flatMap { customerRepo.retrieveCustomer(it) }
            .flatMap { ServerResponse.ok().json().body(Mono.just(it), CustomerPersist::class.java) }
            .doOnError { logger.error("Exception while trying to retrieve a customer record - $it") }
    }
}
