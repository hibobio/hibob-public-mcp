package com.hibob.service.queue

import com.amazonaws.auth.AWSCredentialsProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.hibob.async.notifications.*
import com.hibob.aws.AmazonEnvironment
import com.hibob.kotlin.logging.Logger
import com.hibob.metrics.Metrics
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// startup
@Component
class BobEventsListener(
    @Value("\${bob.topicArn}") topicArn: String,
    @Value("\${aws.region}") region: String,
    @Value("\${environment.local}") isLocal: Boolean,
    amazonEnvironment: AmazonEnvironment,
    @Value("\${bobEvents.queueNamePrefix}") queueNamePrefix: String,
    private val objectMapper: ObjectMapper,
    credentialsProvider: AWSCredentialsProvider,
) {
    private val amazonConfig = AmazonConfig(credentialsProvider, topicArn, amazonEnvironment, region)
    private val facade = AmazonSnsSqsFactory.create(amazonConfig)
    private val queue =
        EventsQueue(
            facade,
            EventsQueueConfiguration(
                queueNamePrefix,
                MessagesFilter.event("company.deleted"),
                messagesToPrefetch = 1,
                deadLetterQueue = !isLocal,
            ),
        )

    private val logger = Logger(javaClass)
    private val timer = Metrics.timer("msteams.event.process")

    @PostConstruct
    fun run() {
        queue.listen { message ->
            require(message != null)
            timer.record {
                val messageBody = MessageParser.extractMessageBody(message)
                runCatching {
                    processMessage(messageBody)
                    message.acknowledge()
                }.onFailure { logger.error("Failed to process message", it) }
            }
        }
    }

    //    TODO - implement deletion of employee and company.
    private fun processMessage(messageBody: String) {
        val message = objectMapper.readTree(messageBody) as ObjectNode
        val companyId = message["companyId"].asLong()
//        companyTokenDao.removeSlackToken(companyId)
    }

    @PreDestroy
    fun stop() {
        queue.stop()
        queue.close()
    }
}
