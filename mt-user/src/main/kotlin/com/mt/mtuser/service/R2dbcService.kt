package com.mt.mtuser.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/25.
 */
@Service
class R2dbcService {

    @Autowired
    protected lateinit var template: R2dbcEntityTemplate

    fun getUpdate(data: Any): Update {
        val dataAccessStrategy: ReactiveDataAccessStrategy = template.dataAccessStrategy
        val columns: OutboundRow = template.dataAccessStrategy.getOutboundRow(data)
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
        return template.dataAccessStrategy
            .converter
            .mappingContext
            .getRequiredPersistentEntity(type)
            .tableName
    }

    fun getQueryById(data: Any): Query {
        val dataAccessStrategy = template.dataAccessStrategy
        val outboundRow = dataAccessStrategy.getOutboundRow(data)
        val identifierColumns = dataAccessStrategy.getIdentifierColumns(data.javaClass)
        if (CollectionUtils.isEmpty(identifierColumns)) {
            throw IllegalStateException("No identifier columns in " + data.javaClass.name + "!")
        }
        val sqlIdentifier = identifierColumns[0]
        val parameter = outboundRow[sqlIdentifier] ?: throw IllegalStateException("主键不能未空")
        return Query.query(
            Criteria.where(dataAccessStrategy.toSql(sqlIdentifier)).`is`(
                parameter.value!!
            )
        )
    }

    fun dynamicUpdate(data: Any): Mono<Int> {
        return template.update(data.javaClass)
            .inTable(getTable(data.javaClass))
            .matching(getQueryById(data))
            .apply(getUpdate(data))
    }

}
