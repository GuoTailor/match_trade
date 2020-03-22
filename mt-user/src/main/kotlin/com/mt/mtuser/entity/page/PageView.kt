package com.mt.mtuser.entity.page

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

inline fun <reified T : Any> getPage(data: Flux<T>, connect: DatabaseClient, pageQuery: PageQuery): Mono<PageView<T>> {
    val pageView = PageView<T>()
    val tableName = T::class.findAnnotation<Table>()?.value ?: T::class.simpleName
    ?: throw IllegalStateException("不支持的匿名类")
    val where = pageQuery.getWhere().let { if (it.isBlank()) "" else " where $it" }
    val count = connect.execute("select count(1) from $tableName $where")
            .map { r, _ -> r.get(0, java.lang.Long::class.java) }.one()
    return Mono.zip(count, data.collectList()) { total, items ->
        pageView.total = total?.toLong()
        pageView.item = items
        pageView.pageNum = pageQuery.pageNum
        pageView.pageSize = pageQuery.pageSize
        pageView
    }
}

