package io.codebrews.kotlinkafkadynamodemo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "application.dynamo")
data class DynamoConfigProperties(
    val customerTableName: String,
    val region: String,
    val endpoint: String
)
