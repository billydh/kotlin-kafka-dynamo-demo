package io.codebrews.kotlinkafkadynamodemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class KafkaDynamoDemoApplication

fun main(args: Array<String>) {
	runApplication<KafkaDynamoDemoApplication>(*args)
}
