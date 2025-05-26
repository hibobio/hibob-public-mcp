package com.hibob.hibobPublicMcp

import com.hibob.authentication.User
import com.hibob.authentication.UserInfo
import com.hibob.authentication.filters.BobAuthFilter
import com.hibob.id.EmployeeId
import com.hibob.rate.limiter.MockedRateLimiterSupport
import com.hibob.rate.limiter.RateLimiter
import com.hibob.service.testinfra.TestUtils
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
internal class ExampleResourceTest : MockedRateLimiterSupport {
    @MockBean
    lateinit var userInfo: UserInfo

    @MockBean
    lateinit var bobAuthFilter: BobAuthFilter

    @MockBean
    override lateinit var rateLimiter: RateLimiter

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val companyId = TestUtils.randomCompanyId()
    private val employeeId = EmployeeId("emp-id")
    private val user = User(companyId, employeeId)

    @BeforeEach
    fun run() {
        doNothing().whenever(bobAuthFilter).filter(any())
        whenever(userInfo.user).thenReturn(user)
        whenever(userInfo.companyId).thenReturn(companyId)
        whenever(userInfo.employeeId).thenReturn(user.employeeId)
        stubRateLimiterExecuteWithRateLimit<Any>()
    }

    @Test
    fun test() {
        val response = restTemplate.getForEntity("/example", ExampleResponse::class.java)
        assertThat(response.statusCode, Matchers.`is`(HttpStatus.OK))
        assertThat(response.body, `is`(ExampleResponse()))
    }
}
