package io.codebrews.kotlinkafkadynamodemo

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient

object MockSchemaRegistry {
    val client = MockSchemaRegistryClient()
}
