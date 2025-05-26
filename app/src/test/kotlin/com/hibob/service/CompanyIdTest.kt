package com.hibob.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hibob.id.CompanyId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.io.StringWriter

internal class CompanyIdTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    data class TestSerialization(val companyId: CompanyId)

    @Test
    fun serialization() {
        val testClass = TestSerialization(CompanyId(9))
        val stream = StringWriter()
        objectMapper.writeValue(stream, testClass)
        assertThat(objectMapper.readTree(stream.toString()), `is`(objectMapper.nodeFactory.objectNode().put("companyId", 9)))
    }

    @Test
    fun deserialization() {
        val stream = StringWriter()
        objectMapper.writeValue(stream, objectMapper.nodeFactory.objectNode().put("companyId", 9))
        val actual = objectMapper.readValue(stream.toString(), TestSerialization::class.java)
        val testClass = TestSerialization(CompanyId(9))
        assertThat(actual, `is`(testClass))
    }
}
