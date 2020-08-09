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
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Update.update
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
    protected lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var stockholderDao: StockholderDao

    @Autowired
    lateinit var r2dbc: R2dbcService

    @Autowired
    lateinit var roleService: RoleService

    fun register(user: User): Mono<Unit> = r2dbc.withTransaction {
        logger.info("register" + user.phone + user.password)
        if (!StringUtils.isEmpty(user.phone) && !StringUtils.isEmpty(user.password)) {
            if (userDao.existsUserByPhone(user.phone!!) == 0) {
                user.passwordEncoder()
                user.id = null
                val newUser = userDao.save(user)
                stockholderDao.save(Stockholder(userId = newUser.id, roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id))
                Unit
            } else throw IllegalStateException("用户已存在")
        } else throw IllegalStateException("请正确填写用户名或密码")
    }

    /**
     * 此方法仅用于实验，对应注册的响应式写法，可能后期替换全部协程。
     * 关于使用协程的不便请看README的注意部分
     */
    @Transactional
    fun registerMono(user: Mono<User>): Mono<Stockholder> {
        return user.filter { !StringUtils.isEmpty(it.phone) && !StringUtils.isEmpty(it.password) }
                .switchIfEmpty(Mono.error(IllegalStateException("请正确填写用户名或密码")))
                .flatMap { mono { userDao.existsUserByPhone(it.phone!!) } }
                .filter { it == 0 }
                .switchIfEmpty(Mono.error(IllegalStateException("用户已存在")))
                .flatMap { user }
                .flatMap { ur ->
                    ur.passwordEncoder()
                    ur.id = null
                    mono { userDao.save(ur) }
                }.flatMap { newUser ->
                    mono { stockholderDao.save(Stockholder(newUser.id, roleService.getRoles().find { it.name == Stockholder.USER }!!.id, null)) }
                }
    }

    suspend fun findById(id: Int) = userDao.findById(id)

    suspend fun save(user: User): Int {
        return connect.update()
                .table(r2dbc.getTable(User::class.java))
                .using(r2dbc.getUpdate(user))
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

    suspend fun findByPhone(phone: String) = userDao.findByPhone(phone)

    suspend fun findByIdIn(ids: List<Int>) = userDao.findByIdIn(ids)

    suspend fun findAllUser(query: PageQuery): PageView<User> {
        return getPage(connect.select()
                .from<User>()
                .matching(query.where())
                .page(query.page())
                .fetch()
                .all(), connect, query)
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
        val data = connect.execute(sql)
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
        return getPage(data, connect, query, sql, "")
    }

    suspend fun getAnalystInfo(id: Int?): Analyst {
        val userId = id ?: BaseUser.getcurrentUser().awaitSingle().id!!
        val roleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        return connect.execute("select mu.*, count(ms.*) as companyCount " +
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
                .matching(where("id").`is`(user.id!!))
                .fetch().awaitRowsUpdated()
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val user = findById(userId)!!
        if (user.matchesPassword(oldPassword)) {
            user.password = user.passwordEncoder(newPassword)
            return connect.update()
                    .table(r2dbc.getTable(User::class.java))
                    .using(update("password", user.password))
                    .matching(where("id").`is`(user.id!!))
                    .fetch().awaitRowsUpdated() > 0
        } else throw IllegalStateException("密码错误")
    }

    suspend fun forgetPassword(user: User): Boolean {
        return connect.update()
                .table(r2dbc.getTable(User::class.java))
                .using(update("password", user.password))
                .matching(where("phone").`is`(user.phone!!))
                .fetch().awaitRowsUpdated() > 0
    }

}
