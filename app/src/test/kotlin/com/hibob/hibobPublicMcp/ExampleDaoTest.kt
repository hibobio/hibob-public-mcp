package com.hibob.hibobPublicMcp

import com.hibob.common.utils.BobDbTest
import com.hibob.id.CompanyId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.random.Random

@BobDbTest
class ExampleDaoTest(@Autowired private val sql: DSLContext) {
    private val dao = ExampleDao(sql)
    val companyId = CompanyId(Random.nextLong())
    val table = ExampleTable.instance

    @BeforeEach
    fun createTable() {
        sql.createTable(table).columns(*table.fields()).execute()
    }

    @AfterEach
    fun dropTable() {
        sql.dropTable(table).execute()
    }

    @Test
    fun createAndRead() {
        dao.createExample(companyId, "Hello, Kotlin!")
        dao.createExample(companyId, "Hello, micro-service!")
        val actual: List<String> = dao.readExample(companyId)
        assertThat(actual, Matchers.containsInAnyOrder("Hello, Kotlin!", "Hello, micro-service!"))
    }
}
