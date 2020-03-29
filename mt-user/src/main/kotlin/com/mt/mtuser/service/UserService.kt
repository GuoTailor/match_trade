package com.mt.mtuser.service

import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Role
import com.mt.mtuser.entity.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class UserService {
    val loggger = LoggerFactory.getLogger(this.javaClass.simpleName)
    @Autowired
    protected lateinit var connect: DatabaseClient
    @Autowired
    private lateinit var userDao: UserDao
    @Autowired
    private lateinit var userRoleDao: UserRoleDao
    @Autowired
    lateinit var dynamicSql: DynamicSqlService

    @Transactional
    fun register(user: User): Mono<ResponseInfo<Unit>> {
        loggger.info(user.phone + user.password)
        return Mono.defer {
            Mono.just(user)
                    .filter { !StringUtils.isEmpty(it.phone) }
                    .filter { !StringUtils.isEmpty(it.password) }
                    .switchIfEmpty(Mono.error(RuntimeException()))
                    .flatMap { userDao.existsUserByPhone(user.phone!!) }
                    .filter { it == 0 }
                    .map { user.passwordEncoder().id = null }
                    .flatMap { userDao.save(user) }
                    .flatMap { userRoleDao.save(Role(it.id, 3 /*todo 角色id写死很危险*/, null)) }
                    .flatMap { Mono.just(ResponseInfo<Unit>(0, "成功")) }
                    .defaultIfEmpty(ResponseInfo<Unit>(1, "用户已存在"))
        }.onErrorReturn(ResponseInfo<Unit>(1, "请正确填写用户名或密码"))
    }

    fun findById(id: Int) = userDao.findById(id)

    fun save(user: User): Mono<Int> {
        return connect.update()
                .table(dynamicSql.getTable(User::class.java))
                .using(dynamicSql.getUpdate(user))
                .matching(where("id").`is`(user.id!!))
                .fetch()
                .rowsUpdated()
    }


}