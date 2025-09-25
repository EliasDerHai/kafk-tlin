package com.example

import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.admin
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.ktor.client.HttpClient
import io.ktor.server.application.*

fun Application.configureDatabases() {
    install(Kafka) {
        schemaRegistryUrl = "http://localhost:7788"
        val pingTopic = TopicName.named("ping")
        topic(pingTopic) {
            partitions = 1
            replicas = 1
            configs {
                messageTimestampType = MessageTimestampType.CreateTime
            }
        }
        common {
            bootstrapServers = listOf("localhost:19092", "localhost:19093")
            retries = 1
            clientId = "ping-client-id"
        }
        admin { }
        producer {
            clientId = "ping-client-id"
        }
        registerSchemas {
            using {
                HttpClient()
            }
        }
    }
}
