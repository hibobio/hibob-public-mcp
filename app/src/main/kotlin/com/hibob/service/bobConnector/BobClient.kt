package com.hibob.service.bobConnector

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hibob.BobEmployee
import com.hibob.Principal
import com.hibob.authentication.UserInfo
import com.hibob.authentication.s2s.ServerToServerSecurityConsumer
import com.hibob.id.CompanyId
import com.hibob.kotlin.logging.Logger
import jakarta.inject.Provider
import jakarta.ws.rs.core.Response
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class BobClient(
    private val objectMapper: ObjectMapper,
    @Value("\${bob.url}") private val bobUrl: String,
    private val httpClient: CloseableHttpClient,
    private val serverToServerSecurity: ServerToServerSecurityConsumer,
    private val userInfoProvider: Provider<UserInfo>,
) {
    private val logger = Logger(javaClass)

    private fun RequestBuilder.sign(principal: Principal): RequestBuilder = serverToServerSecurity.sign(this, principal)

    private fun RequestBuilder.sign(companyId: CompanyId): RequestBuilder =
        serverToServerSecurity.sign(this, BobEmployee(companyId.value, ""))

    private fun RequestBuilder.withJson(json: JsonNode): RequestBuilder {
        val entity = StringEntity(objectMapper.writeValueAsString(json), ContentType.APPLICATION_JSON)
        return this.setEntity(entity)
    }

    private fun <T> RequestBuilder.parseJson(f: (JsonNode) -> T) =
        httpClient.execute(this.build()).use { r -> f(objectMapper.readTree(r.entity.content)) }

    fun get(relativeUrl: String, principal: Principal? = null): JsonNode {
        return RequestBuilder.get("$bobUrl$relativeUrl")
            .sign(principal ?: userInfoProvider.get().principal)
            .parseJson { it }
    }

    fun post(relativeUrl: String, body: JsonNode, principal: Principal?): JsonNode {
        return RequestBuilder.post("$bobUrl$relativeUrl")
            .withJson(body)
            .sign(principal ?: userInfoProvider.get().principal)
            .parseJson { it }
    }

    fun getUserSettings(principal: Principal): UserSettings {
        return RequestBuilder.get("$bobUrl$USER_SETTINGS_PATH")
            .sign(principal)
            .parseJson()
    }

    private inline fun <reified T> RequestBuilder.parseJson(): T {
        logger.info("Making a request to bob to this URI: ${this.uri}")
        httpClient.execute(this.build()).use { response ->
            if (response.statusLine.statusCode == Response.Status.OK.statusCode) {
                return objectMapper.readValue(response.entity.content)
            } else {
                throw RuntimeException("Got ${response.statusLine.statusCode} from url: ${this.uri}")
            }
        }
    }

    companion object {
        private const val SEARCH_PATH = "/api/internal/employee/search"
        private const val USER_SETTINGS_PATH = "/api/user/settings"
    }
}

data class UserSettings(
    val language: String?,
    val timezone: String?,
    val dateFormat: String?,
    val timeFormat: String?,
)
