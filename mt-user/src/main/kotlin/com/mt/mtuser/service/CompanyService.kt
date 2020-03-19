package com.mt.mtuser.service

import com.mt.mtuser.common.page.PageQuery
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.entity.Company
import io.r2dbc.postgresql.api.PostgresqlConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

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
    private lateinit var connect: PostgresqlConnection

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

    fun findAllByQuery(query: PageQuery): Flux<Company> {
        return connect.createStatement("select id, name, room_count, mode, create_time from mt_company " +
                        query.where().query().limit().build())
                .execute()
                .flatMap {
                    it.map { t, u ->
                        val company = Company()
                        company.id = t.get("id", Integer::class.java)?.toInt()
                        company.name = t.get("name", String::class.java)
                        company.roomCount = t.get("room_count", Integer::class.java)?.toInt()
                        company.mode = t.get("mode", String::class.java)
                        company.createTime = t.get("create_time", Date::class.java)
                        company
                    }
                }
    }

}