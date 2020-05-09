package com.mt.mtengine.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.mapping.SettableValue
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

/**
 * Created by gyh on 2020/3/25.
 */
@Service
class R2dbcService {
    @Autowired
    lateinit var dataAccessStrategy: ReactiveDataAccessStrategy

    @Autowired
    lateinit var transactionManager: R2dbcTransactionManager

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

    fun dynamicUpdate(data: Any): DatabaseClient.UpdateMatchingSpec {
        return connect.update()
                .table(getTable(data.javaClass))
                .using(getUpdate(data))
    }

    /**
     * 创建用事务包装的冷[mono][Mono]，它将在协程中运行给定的[block]并发出其结果
     * 如果[block]结果为null，则不带任何值调用[MonoSink.success]。取消订阅将取消运行协程。
     */
    fun <T> withTransaction(block: () -> Mono<T>): Mono<T> {
        val operator = TransactionalOperator.create(transactionManager)
        return operator.transactional(block())
    }
}