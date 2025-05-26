package com.hibob.kotlintemplate

import com.hibob.authentication.Authentication
import com.hibob.authentication.Secured
import com.hibob.rate.limiter.RateLimit
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.springframework.stereotype.Component

@Component
@Path("/example")
@Secured(authentication = Authentication.LoggedInUser)
class ExampleResource {
    @GET
    @RateLimit("example-resource-rate-limit", 5)
    @Produces(MediaType.APPLICATION_JSON)
    fun example(): Response {
        return Response.ok(ExampleResponse()).build()
    }
}

data class ExampleResponse(val data: String = "Hello World")
