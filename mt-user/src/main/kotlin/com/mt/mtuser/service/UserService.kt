package com.mt.mtuser.service

import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.dao.StockholderDao
import com.mt.mtuser.entity.Analyst
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.User
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update.update
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class UserService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    protected lateinit var template: R2dbcEntityTemplate

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var stockholderDao: StockholderDao

    @Autowired
    lateinit var r2dbc: R2dbcService

    @Autowired
    lateinit var roleService: RoleService

    @Transactional(rollbackFor = [Exception::class])
    suspend fun register(user: User)  {
        logger.info("register" + user.phone + user.password)
        if (StringUtils.hasLength(user.phone) && StringUtils.hasLength(user.password)) {
            if (userDao.existsUserByPhone(user.phone!!) == 0) {
                user.passwordEncoder()
                user.id = null
                val newUser = userDao.save(user)
                if (user.phone == "123") {
                    throw IllegalStateException("用户已存在")
                }
                stockholderDao.save(Stockholder(userId = newUser.id, roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id))
            } else throw IllegalStateException("用户已存在")
        } else throw IllegalStateException("请正确填写用户名或密码")
    }

    suspend fun findById(id: Int) = userDao.findById(id)

    suspend fun save(user: User): Int {
        return template.update<User>()
                .matching(Query.query(where("id").`is`(user.id!!)))
                .apply(r2dbc.getUpdate(user))
                .awaitSingle()
    }

    suspend fun count() = userDao.count()

    /**
     * 判断用户是否存在，存在为true，不存在为false
     */
    suspend fun existsUserByPhone(phone: String) = userDao.existsUserByPhone(phone) > 0

    suspend fun findByPhone(phone: String) = userDao.findByPhone(phone)

    suspend fun findByIdIn(ids: List<Int>) = userDao.findByIdIn(ids)

    suspend fun findAllUser(query: PageQuery): PageView<User> {
        return getPage(template.select<User>()
                .matching(Query.query(query.where()).with(query.page()))
                .all(), template, query)
    }

    /**
     * 添加一个企业观察员
     */
    suspend fun addAnalystRole(user: User) {
        user.passwordEncoder()
        if (userDao.findByPhone(user.phone!!) != null) error("用户已存在：${user.phone}")
        val newUser = userDao.save(user)
        stockholderDao.save(newUser.id!!, roleService.getRoles().find { it.name == Stockholder.ANALYST }?.id!!, null)
    }

    /**
     * 获取全部的观察员
     */
    suspend fun getAllAnalystRole(query: PageQuery): PageView<Analyst> {
        val roleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        val where = query.where("mu").toString()
        val sqlWhere = "mu.id = ms.user_id and ms.role_id = $roleId" + if (where.isNotBlank()) " and $where" else ""
        val sql = "select mu.*, count(ms.*) as companyCount " +
                " from mt_user mu, mt_stockholder ms " +
                " where $sqlWhere" +
                " GROUP BY mu.id ${query.toPageSql()}"
        val data = template.databaseClient.sql(sql)
                .map { r, _ ->
                    val analyst = Analyst()
                    analyst.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    analyst.phone = r.get("phone", String::class.java)
                    analyst.nickName = r.get("nick_name", String::class.java)
                    analyst.idNum = r.get("id_num", String::class.java)
                    analyst.userPhoto = r.get("user_photo", String::class.java)
                    analyst.createTime = r.get("create_time", LocalDateTime::class.java)
                    // 减一用于去除创建观察员角色时创建的默认的角色信息,且不能用company_id is noe null 应为公司为空也应该查询出来观察员
                    analyst.companyCount = r.get("companyCount", java.lang.Integer::class.java)?.toInt()?.minus(1) ?: 0
                    analyst
                }.all()
        return getPage(data, template, query, sql, "")
    }

    suspend fun getAnalystInfo(id: Int?): Analyst {
        val userId = id ?: BaseUser.getcurrentUser().awaitSingle().id!!
        val roleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        return template.databaseClient.sql("select mu.*, count(ms.*) as companyCount " +
                " from mt_user mu, mt_stockholder ms " +
                " where mu.id = ms.user_id and ms.role_id = :roleId and ms.user_id = :userId" +
                " GROUP BY mu.id ")
                .bind("userId", userId)
                .bind("roleId", roleId)
                .map { r, _ ->
                    val analyst = Analyst()
                    analyst.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    analyst.phone = r.get("phone", String::class.java)
                    analyst.nickName = r.get("nick_name", String::class.java)
                    analyst.idNum = r.get("id_num", String::class.java)
                    analyst.userPhoto = r.get("user_photo", String::class.java)
                    analyst.createTime = r.get("create_time", LocalDateTime::class.java)
                    // 减一用于去除创建观察员角色时创建的默认的角色信息,且不能用company_id is noe null 应为公司为空也应该查询出来观察员
                    analyst.companyCount = r.get("companyCount", java.lang.Integer::class.java)?.toInt()?.minus(1) ?: 0
                    analyst
                }.awaitOneOrNull() ?: error("用户不是观察员：$userId")
    }

    /**
     * 删除一个观察员
     */
    suspend fun deleteAnalyst(stockholderId: Int) {
        userDao.deleteById(stockholderId)
    }

    /**
     * 更新一个企业观察员
     */
    suspend fun updateAnalyst(user: User) {
        user.passwordEncoder()
        r2dbc.dynamicUpdate(user)
                .awaitSingle()
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val user = findById(userId)!!
        if (user.matchesPassword(oldPassword)) {
            user.password = user.passwordEncoder(newPassword)
            return template.update<User>()
                    .matching(Query.query(where("id").`is`(user.id!!)))
                    .apply(update("password", user.password))
                    .awaitSingle() > 0
        } else throw IllegalStateException("密码错误")
    }

    suspend fun forgetPassword(user: User): Boolean {
        return template.update<User>()
                .matching(Query.query(where("phone").`is`(user.phone!!)))
                .apply(update("password", user.password))
                .awaitSingle() > 0
    }

}
