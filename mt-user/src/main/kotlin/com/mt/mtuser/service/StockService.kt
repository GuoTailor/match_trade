package com.mt.mtuser.service

import com.mt.mtuser.dao.StockDao
import com.mt.mtuser.entity.Stock
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/22.
 */
@Service
class StockService {
    @Autowired
    private lateinit var stockDao: StockDao
    @Autowired
    private lateinit var connect: DatabaseClient

    suspend fun findById(id: Int) = stockDao.findById(id)

    suspend fun findAllByQuery(query: PageQuery): PageView<Stock> {
        return getPage(connect.select()
                .from<Stock>()
                .matching(query.where())
                .page(query.page())
                .fetch()
                .all()
                , connect, query)
    }

    internal suspend fun save(stock: Stock) = stockDao.save(stock)

    internal suspend fun deleteById(id: Int) = stockDao.deleteById(id)
}