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

    suspend fun count(): Long {
        return companyDao.count()
    }

    suspend fun save(company: Company): Company {
        return companyDao.save(company)
    }

    suspend fun deleteById(id: Int) = companyDao.deleteById(id)

    suspend fun update(company: Company) = companyDao.save(company)

    suspend fun findById(id: Int) = companyDao.findById(id)

    suspend fun findAll() = companyDao.findAll()

    suspend fun findAllByQuery(query: PageQuery): PageView<Company> {
        return getPage(connect.select()
                .from<Company>()
                .matching(query.where())
                .page(query.page())
                .fetch()
                .all()
                , connect, query)
    }

}