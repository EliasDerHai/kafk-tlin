package com.example

import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.admin
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.consumer
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.ktor.client.HttpClient
import io.ktor.server.application.*
import io.ktor.server.config.tryGetString
import java.time.Instant
import java.time.ZoneId

fun Application.configureDatabases() {
    install(Kafka) {
         schemaRegistryUrl = environment.config.tryGetString("ktor.kafka.schema.registry.url")
            ?: "http://localhost:7788"
        val pingTopic = TopicName.named("ping")
        topic(pingTopic) {
            partitions = 1
            replicas = 1
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }
        common {
            bootstrapServers = environment.config.tryGetString("ktor.kafka.common.bootstrap.servers")
                ?.split(",")?.map { it.trim() }
                ?: listOf("localhost:19092", "localhost:19093")
            retries = 1
            clientId = "pong-client-id"
        }
        admin { }
        producer {
            clientId = "pong-client-id"
        }
        consumer {
            groupId = "pong-group-id"
            clientId = "pong-client-id-override"
        }
        consumerConfig {
           consumerRecordHandler(pingTopic) { record ->
               val millis: Long = (record.value().get(0) as Number).toLong()
               println("Received ping: ${record.value()} (-> ${
                   Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
               })")
               println("From partition: ${record.partition()}, offset: ${record.offset()}")
           }
        }
        registerSchemas {
            using {
                HttpClient()
            }
        }
    }
}

