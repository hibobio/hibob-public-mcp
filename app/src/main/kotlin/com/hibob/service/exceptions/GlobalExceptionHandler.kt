package com.hibob.service.exceptions

import com.fasterxml.jackson.annotation.JsonInclude
import com.hibob.authentication.UserInfo
import com.hibob.kotlin.logging.Logger
import com.hibob.service.common.MessagesApi
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.springframework.stereotype.Component
import java.util.*

@Component
@Provider
class GlobalExceptionHandler(private val messagesApi: MessagesApi, private val userInfoProvider: jakarta.inject.Provider<UserInfo>) :
    ExceptionMapper<RuntimeException> {
    private val logger = Logger(javaClass)

    override fun toResponse(exception: RuntimeException): Response {
        return when (exception) {
            is BadInputException -> toTranslatedErrorResponse(exception, Response.Status.BAD_REQUEST)
            is NotFoundException -> toTranslatedErrorResponse(exception, Response.Status.NOT_FOUND)
            is UnauthorizedException -> toTranslatedErrorResponse(exception, Response.Status.UNAUTHORIZED)
            is ForbiddenException -> toTranslatedErrorResponse(exception, Response.Status.FORBIDDEN)
            is WebApplicationException -> { // handles Dropwizard exceptions like invalid path param, method not allowed etc.
                logger.warn(exception.message.orEmpty(), exception)
                exception.response
            }
            else -> {
                val errorMessage = "An unexpected error occurred"
                logger.error(exception.message.orEmpty(), exception)
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceResponse.error(errorMessage))
                    .type(MediaType.APPLICATION_JSON).build()
            }
        }
    }

    private fun toTranslatedErrorResponse(exception: TranslatedException, status: Response.Status): Response {
        val translatedErrorMessage = getTranslatedErrorMessage(exception)
        logger.warn(translatedErrorMessage, exception)
        return Response.status(status).entity(ServiceResponse.error(translatedErrorMessage))
            .type(MediaType.APPLICATION_JSON).build()
    }

    private fun getTranslatedErrorMessage(exception: TranslatedException): String {
        val messages = userInfoProvider.get()?.let { messagesApi.forUser(it.user) } ?: messagesApi.forLocale(Locale.ENGLISH)
        return messages.format(exception.key, *exception.args)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ServiceResponse(val success: Boolean, val errorMessage: String? = null, val info: Any? = null) {
    companion object {
        fun success() = ServiceResponse(success = true)

        fun error(errorMessage: String, info: Any? = null) = ServiceResponse(success = false, errorMessage = errorMessage, info = info)
    }
}
