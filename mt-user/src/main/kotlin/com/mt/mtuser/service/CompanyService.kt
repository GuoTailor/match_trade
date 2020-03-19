package com.mt.mtuser.service

import com.mt.mtuser.common.page.PageQuery
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.entity.Company
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class CompanyService {
    @Autowired
    private lateinit var companyDao: CompanyDao

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
        return companyDao.findAllByQuery(query.buildSubSql())
    }

}