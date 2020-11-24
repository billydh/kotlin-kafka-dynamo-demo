package io.codebrews.kotlinkafkadynamodemo

import io.confluent.kafka.serializers.KafkaAvroDeserializer

class MockKafkaAvroDeserializer : KafkaAvroDeserializer(MockSchemaRegistry.client)
