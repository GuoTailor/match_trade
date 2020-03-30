package com.mt.mtuser.entity.page

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.mapping.Table
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.reflect.full.findAnnotation

/**
 * Created by gyh on 2020/3/21.
 */
class PageView<T : Any> {
    // 当前页号
    var pageNum = 0

    // 每页的数量
    var pageSize = 0

    // 总记录数
    var total: Long? = 0
    var item: List<T>? = null
}

suspend inline fun <reified T : Any> getPage(data: Flux<T>, connect: DatabaseClient, pageQuery: PageQuery): PageView<T> {
    val pageView = PageView<T>()
    val tableName = T::class.findAnnotation<Table>()?.value ?: T::class.simpleName
    ?: throw IllegalStateException("不支持的匿名类")
    val where = pageQuery.getWhere().let { if (it.isBlank()) "" else " where $it" }
    val count = connect.execute("select count(1) from $tableName $where")
            .map { r, _ -> r.get(0, java.lang.Long::class.java) }.one().awaitSingle()
    pageView.total = count?.toLong()
    pageView.item = data.collectList().awaitSingle()
    pageView.pageNum = pageQuery.pageNum
    pageView.pageSize = pageQuery.pageSize
    return pageView
}

