package com.hibob.service.security

import com.hibob.db.jooq.JooqTable
import com.hibob.db.jooq.TableField
import com.hibob.encryption.EncryptionKey
import com.hibob.encryption.EncryptionKeyStorageSpi
import com.hibob.id.CompanyId
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.springframework.stereotype.Component

@Component
class EncryptionKeyDao(private val sql: DSLContext) : EncryptionKeyStorageSpi {
    private val t = EncryptionKeyTable.instance

    override fun store(companyId: CompanyId, key: EncryptionKey) {
        sql.insertInto(t)
            .set(t.companyId, companyId)
            .set(t.encryptedKey, key.encryptedKey)
            .execute()
        Unit
    }

    override fun retrieve(companyId: CompanyId): EncryptionKey? =
        sql.select(t.encryptedKey)
            .from(t).where(t.companyId.eq(companyId))
            .fetchOne(t.encryptedKey)?.let { EncryptionKey(it) }
}

class EncryptionKeyTable : JooqTable("encryption_key") {
    val companyId = createCompanyIdField()
    val encryptedKey: TableField<ByteArray> = createField(DSL.name("encrypted_key"), SQLDataType.BLOB)

    companion object {
        val instance = EncryptionKeyTable()
    }
}
