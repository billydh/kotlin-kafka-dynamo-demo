package io.codebrews.kotlinkafkadynamodemo

import io.confluent.kafka.serializers.KafkaAvroSerializer

class MockKafkaAvroSerializer : KafkaAvroSerializer(MockSchemaRegistry.client)
