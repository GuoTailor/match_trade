package com.mt.mtuser.service

import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Role
import com.mt.mtuser.entity.User
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class UserService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    protected lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var userRoleDao: UserRoleDao

    @Autowired
    lateinit var dynamicSql: DynamicSqlService

    fun register(user: User): Mono<Unit> = dynamicSql.withTransaction {
        logger.info("register" + user.phone + user.password)
        if (!StringUtils.isEmpty(user.phone) && !StringUtils.isEmpty(user.password)) {
            if (userDao.existsUserByPhone(user.phone!!) == 0) {
                user.passwordEncoder()
                user.id = null
                val newUser = userDao.save(user)
                userRoleDao.save(Role(newUser.id, 3 /*todo 角色id写死很危险*/, null))
                Unit
            } else throw IllegalStateException("用户已存在")
        } else throw IllegalStateException("请正确填写用户名或密码")
    }

    suspend fun findById(id: Int) = userDao.findById(id)

    suspend fun save(user: User): Int {
        return connect.update()
                .table(dynamicSql.getTable(User::class.java))
                .using(dynamicSql.getUpdate(user))
                .matching(where("id").`is`(user.id!!))
                .fetch()
                .rowsUpdated()
                .awaitSingle()
    }

    suspend fun count() = userDao.count()

    /**
     * 判断用户是否存在，存在为true，不存在为false
     */
    suspend fun existsUserByPhone(phone: String) = userDao.existsUserByPhone(phone) > 0


    suspend fun findByIdIn(ids : List<Int>) = userDao.findByIdIn(ids)

    suspend fun findAll() = userDao.findAll()
}