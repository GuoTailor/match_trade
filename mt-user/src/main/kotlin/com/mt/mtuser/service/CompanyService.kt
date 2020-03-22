package com.mt.mtuser.service

import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.entity.Company
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class CompanyService {
    @Autowired
    private lateinit var companyDao: CompanyDao

    /**
     * 无赖使用{@link PostgresqlConnection}
     * 由于r2dbc的sql语句中不支持占位符，
     * 故如果要代码动态生成sql语句只能使用代码形式手动拼接字符串
     */
    @Autowired
    private lateinit var connect: DatabaseClient

    fun count(): Mono<Long> {
        return companyDao.count()
    }

    fun save(company: Company): Mono<Company> {
        return companyDao.save(company)
    }

    fun deleteById(id: Int) = companyDao.deleteById(id)

    fun update(company: Company) = companyDao.save(company)

    fun findById(id: Int) = companyDao.findById(id)

    fun findAll() = companyDao.findAll()

    fun findAllByQuery(query: PageQuery): Mono<PageView<Company>> {
        return getPage(connect.select()
                .from<Company>()
                .matching(query.where())
                .page(query.page())
                .fetch()
                .all()
                , connect, query)
    }

}