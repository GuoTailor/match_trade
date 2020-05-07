package com.mt.mtuser.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.mapping.SettableValue
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.util.*

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

    fun getAllColumns(data: Any) {
        val list = LinkedList<String>()
        val columns:List<SqlIdentifier> = dataAccessStrategy.getAllColumns(data.javaClass)
        for (column in columns) {
            println(dataAccessStrategy.toSql(column))
        }
    }

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

    // -----------====目前我能想到的较好的办法对协程事务的支持，也许有其他方法，期待Spring Framework 5.3的发布====----------
    // 见 https://github.com/spring-projects/spring-framework/issues/23575

    /**
     * 创建用事务包装的冷[mono][Mono]，它将在协程中运行给定的[block]并发出其结果
     * 如果[block]结果为null，则不带任何值调用[MonoSink.success]。取消订阅将取消运行协程。
     */
    fun <T> withTransaction(block: suspend CoroutineScope.() -> T?): Mono<T> {
        val operator = TransactionalOperator.create(transactionManager)
        val result = mono(block = block)
        return operator.transactional(result)
    }

    /**
     * 将给定的流转换为冷[flux][Flux],并用事务包装。当[Flux]被用户丢弃时，原有的[Flow]将被取消。
     * 此函数与[ReactorContext]集成，请参阅其文档了解更多详细信息。
     */
    fun <T: Any> withTransaction(flow: Flow<T>): Flux<T> {
        val operator = TransactionalOperator.create(transactionManager)
        val result = flow.asFlux()
        return operator.transactional(result)
    }

    /**
     * 存在两次转换，推荐使用[withTransaction]的[Mono]版本
     */
    suspend fun <T> withTransactionOnCoroutine(block: suspend CoroutineScope.() -> T?): T = withTransaction(block).awaitSingle()

    /**
     * 存在两次转换，推荐使用[withTransaction]的[Flux]版本
     */
    suspend fun <T: Any> withTransactionOnCoroutine(flow: Flow<T>): Flow<T> = withTransaction(flow).asFlow()
}