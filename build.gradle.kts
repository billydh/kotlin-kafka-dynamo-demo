import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
	id("org.springframework.boot") version "2.2.2.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
}

group = "io.codebrews"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
	jcenter()
	maven {
		url = uri("http://packages.confluent.io/maven/")
	}
}

buildscript {
	repositories {
		jcenter()
	}

	dependencies {
		classpath("com.commercehub.gradle.plugin:gradle-avro-plugin:0.17.0")
	}
}

apply(plugin = "com.commercehub.gradle.plugin.avro")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("software.amazon.awssdk:dynamodb:2.10.63")

	implementation("org.apache.avro:avro:1.9.2")

	implementation("io.projectreactor.kafka:reactor-kafka:1.2.2.RELEASE")
	implementation("io.confluent:kafka-avro-serializer:5.4.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.projectreactor:reactor-test")
}

configure<DependencyManagementExtension> {
	imports {
		mavenBom("software.amazon.awssdk:bom:2.10.42")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}

	dependsOn("generateAvroJava")
}
