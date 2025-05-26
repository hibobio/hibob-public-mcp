package com.hibob.service.testinfra

import com.hibob.id.CompanyId
import com.hibob.id.EmployeeId
import org.apache.commons.lang3.RandomStringUtils
import kotlin.random.Random

class TestUtils {
    companion object {
        fun randomCompanyId(): CompanyId = CompanyId(Random.nextLong())

        fun randomEmployeeId(): EmployeeId = EmployeeId(RandomStringUtils.randomAlphanumeric(5))
    }
}
