package com.hibob.service.version

import com.fasterxml.jackson.databind.ObjectMapper
import com.hibob.authentication.sharedsecret.SharedSecretAuthenticator
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.springframework.stereotype.Component

@Component
@Path("/version-info")
class VersionController(private val objectMapper: ObjectMapper) {
    companion object {
        private const val SHARED_SECRET_ENV_VAR_NAME = "VERSION_API_SHARED_SECRET"
        private const val DD_VERSION_ENV_VAR_NAME = "DD_VERSION"
    }

    private val versionNumber = System.getenv()[DD_VERSION_ENV_VAR_NAME]
    private val secretAuthenticator = SharedSecretAuthenticator()

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getVersion(
        @Context headers: HttpHeaders,
    ): Response {
        return secretAuthenticator.verify("", headers.requestHeaders, "/version-info", SHARED_SECRET_ENV_VAR_NAME)
            ?.let {
                val objectNode =
                    objectMapper.nodeFactory.objectNode()
                        .put("version", versionNumber)

                Response.ok(objectNode).build()
            } ?: run {
            Response.status(Response.Status.UNAUTHORIZED).build()
        }
    }
}
