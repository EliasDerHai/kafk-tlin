package com.example

import io.ktor.server.application.*
import kotlinx.coroutines.*
import io.github.flaxoos.ktor.server.plugins.kafka.kafkaProducer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord

val schemaJson = """
{
  "type":"record",
  "name":"PingMessage",
  "namespace":"com.example",
  "fields":[
    {"name":"timestamp","type":"long"}
  ]
}
""".trimIndent()
val schema = Schema.Parser().parse(schemaJson)

fun Application.configurePingScheduler() {
    launch {
        while (true) {
            val rec: GenericRecord = GenericData.Record(schema).apply {
                put("timestamp", System.currentTimeMillis())
            }
            val record = ProducerRecord("ping", java.util.UUID.randomUUID().toString(), rec)
            kafkaProducer!!.send(record)
            delay(1000)
        }
    }
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
    configurePingScheduler()
}
