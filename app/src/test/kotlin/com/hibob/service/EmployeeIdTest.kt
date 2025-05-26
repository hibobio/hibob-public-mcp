package com.hibob.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hibob.id.EmployeeId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.io.StringWriter

internal class EmployeeIdTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    data class TestSerialization(val employeeId: EmployeeId)

    @Test
    fun serialization() {
        val testClass = TestSerialization(EmployeeId("abc12345567"))
        val stream = StringWriter()
        objectMapper.writeValue(stream, testClass)
        assertThat(objectMapper.readTree(stream.toString()), `is`(objectMapper.nodeFactory.objectNode().put("employeeId", "abc12345567")))
    }

    @Test
    fun deserialization() {
        val stream = StringWriter()
        objectMapper.writeValue(stream, objectMapper.nodeFactory.objectNode().put("employeeId", "abc12345567"))
        val actual = objectMapper.readValue(stream.toString(), TestSerialization::class.java)
        val testClass = TestSerialization(EmployeeId("abc12345567"))
        assertThat(actual, `is`(testClass))
    }
}
