package com.mt.mtuser.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.mapping.SettableValue
import org.springframework.data.r2dbc.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/25.
 */
@Service
class DynamicSqlService {
    @Autowired
    lateinit var dataAccessStrategy: ReactiveDataAccessStrategy
    @Autowired
    protected lateinit var connect: DatabaseClient

    fun getUpdate(data: Any): Update {
        val columns: MutableMap<SqlIdentifier, SettableValue> = dataAccessStrategy.getOutboundRow(data)
        val ids = dataAccessStrategy.getIdentifierColumns(data.javaClass)
        check(ids.isNotEmpty()) { "No identifier columns in " + data.javaClass.name + "!" }
        columns.remove(ids[0]) // do not update the Id column.
        var update: Update? = null
        for (column in columns.keys) {
            if (columns[column]?.value != null) {
                update = update?.set(dataAccessStrategy.toSql(column), columns[column])
                        ?: Update.update(dataAccessStrategy.toSql(column), columns[column])
            }
        }
        return update ?: throw IllegalStateException("没有可更新的字段")
    }

    fun getTable(type: Class<*>): SqlIdentifier {
        return dataAccessStrategy
                .converter
                .mappingContext
                .getRequiredPersistentEntity(type)
                .tableName
    }

    suspend fun dynamicUpdate(data: Any): DatabaseClient.UpdateMatchingSpec {
        return connect.update()
                .table(getTable(data.javaClass))
                .using(getUpdate(data))
    }
}