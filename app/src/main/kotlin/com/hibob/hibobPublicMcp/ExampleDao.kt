package com.hibob.hibobPublicMcp

import com.hibob.db.jooq.JooqTable
import com.hibob.id.CompanyId
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class ExampleDao(private val sql: DSLContext) {
    private val table = ExampleTable.instance

    fun checkConnection(): Boolean = sql.selectOne().execute() > 0

    fun createExample(companyId: CompanyId, data: String) {
        sql.insertInto(table).columns(table.companyId, table.data)
            .values(companyId, data).execute()
    }

    fun readExample(companyId: CompanyId): List<String> =
        sql.select(table.data).from(table).where(table.companyId.eq(companyId)).fetch(table.data)
}

class ExampleTable(tableName: String) : JooqTable(tableName) {
    val id = createBigIntField("id")
    val companyId = createCompanyIdField("company_id")
    val data = createVarcharField("data")

    companion object {
        val instance = ExampleTable("example")
    }
}
