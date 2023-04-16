package com.mt.mtuser.service

import com.mt.mtuser.dao.StockDao
import com.mt.mtuser.entity.Stock
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/22.
 */
@Service
class StockService {
    @Autowired
    private lateinit var stockDao: StockDao

    @Autowired
    private lateinit var template: R2dbcEntityTemplate

    suspend fun findById(id: Int) = stockDao.findById(id)

    fun findAll() = stockDao.findAll()

    fun findByCompanyId(companyId: Int) = stockDao.findByCompanyId(companyId)

    suspend fun findByName(stockName: String) = stockDao.findByName(stockName)

    suspend fun findAllByQuery(query: PageQuery, companyId: Int): PageView<Stock> {
        val where = query.where().and("company_id").`is`(companyId)
        return getPage(
            template.select<Stock>()
                .matching(query(where).with(query.page()))
                .all(), template, query, where
        )
    }

    internal suspend fun save(stock: Stock) = stockDao.save(stock)

    internal suspend fun deleteById(id: Int) = stockDao.deleteById(id)
}