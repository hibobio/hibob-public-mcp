package com.hibob

import org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        FlywayAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class,
        SecurityAutoConfiguration::class,
        JmxEndpointAutoConfiguration::class,
    ],
)
class HibobPublicMcpApplication

fun main(args: Array<String>) {
    runApplication<HibobPublicMcpApplication>(*args)
}
