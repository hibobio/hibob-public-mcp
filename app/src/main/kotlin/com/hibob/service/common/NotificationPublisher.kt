package com.hibob.service.common

import com.amazonaws.auth.AWSCredentialsProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.hibob.async.notifications.AmazonConfig
import com.hibob.async.notifications.AmazonSnsSqsFactory
import com.hibob.authentication.User
import com.hibob.aws.AmazonEnvironment
import com.hibob.id.CompanyId
import com.hibob.id.EmployeeId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NotificationPublisher(
    @Value("\${userNotifications.topicArn}") topicArn: String,
    @Value("\${aws.region}") region: String,
    amazonEnvironment: AmazonEnvironment,
    credentialsProvider: AWSCredentialsProvider,
    private val objectMapper: ObjectMapper,
) {
    private val facade =
        AmazonSnsSqsFactory.create(
            AmazonConfig(
                credentialsProvider,
                topicArn,
                amazonEnvironment,
                region,
            ),
        )

    private val emailNotificationType = "email"

    fun sendEmail(user: User, message: EmailNotification) {
        val messageAsString = objectMapper.writeValueAsString(MessageWrapperUser(user.companyId, user.employeeId, message))
        val publishRequest = facade.createPublishRequest(emailNotificationType, messageAsString)
        facade.sns.publish(publishRequest)
    }
}

// These data classes are published to SNS so the names of the fields conform to our convention. Do not change them
data class MessageWrapperUser(val companyId: CompanyId, val employeeId: EmployeeId, val message: Any)

data class EmailNotification(
    val from: String,
    val subject: String,
    val body: EmailNotificationBody,
    val headers: Map<String, String> = emptyMap(),
)

data class EmailNotificationBody(val title: String, val content: String, val buttons: List<EmailNotificationButton>)

data class EmailNotificationButton(val text: String, val linkType: String, val link: String) {
    companion object {
        private const val RELATIVE_LINK_TYPE = "relative"
        private const val ABSOLUTE_LINK_TYPE = "absolute"

        fun relative(text: String, link: String) = EmailNotificationButton(text, RELATIVE_LINK_TYPE, link)

        fun absolute(text: String, link: String) = EmailNotificationButton(text, ABSOLUTE_LINK_TYPE, link)
    }
}
