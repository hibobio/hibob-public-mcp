package com.hibob.service.db

import com.hibob.db.jooq.JooqTable
import com.hibob.db.jooq.PostgresUtils
import org.apache.commons.lang3.RandomStringUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PostgresUtilsTest(@Autowired private val sql: DSLContext) {
    private val tableName = RandomStringUtils.randomAlphanumeric(5)
    private val t = TestTable(tableName)

    @BeforeEach
    fun setup() {
        sql.createTable(t)
            .column(t.text)
            .constraint(DSL.constraint("${tableName}_idx").unique(t.text))
            .execute()
    }

    @AfterEach
    fun dropTable() {
        sql.dropTable(t).execute()
    }

    @Test
    fun isUniqueConstraintError() {
        sql.insertInto(t).set(t.text, "value").execute()
        try {
            sql.insertInto(t).set(t.text, "value").execute()
            fail("Unique constraint didn't work")
        } catch (e: Throwable) {
            if (!PostgresUtils.isUniqueConstraintError(e)) {
                fail("Failed to detect unique constraint violation", e)
            }
        }
    }
}

class TestTable(tableName: String) : JooqTable(tableName) {
    val text = createVarcharField("text")
}
