package com.mt.mtuser.service

import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.PositionsDao
import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class CompanyService {
    @Autowired
    private lateinit var companyDao: CompanyDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var positionsDao: PositionsDao

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var r2dbcService: R2dbcService

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

    /**
     * 创建公司，顺便创建一支和公司名同名的股票
     */
    suspend fun save(company: Company): Company {
        val newCompany = companyDao.save(company)
        val stock = Stock(companyId = newCompany.id, name = newCompany.name)
        stockService.save(stock)
        return newCompany
    }

    suspend fun deleteById(id: Int) = companyDao.deleteById(id)

    suspend fun update(company: Company) = companyDao.save(company)

    suspend fun findCompany(id: Int) = companyDao.findById(id)

    suspend fun findCompany(query: PageQuery): PageView<Company> {
        val roles = roleService.getCompanyList()
        return getPage(connect.select()
                .from<Company>()
                .matching(query.where().and("id").`in`(roles))
                .page(query.page())
                .fetch()
                .all()
                , connect, query)

    }

    suspend fun getAllShareholder(query: PageQuery): PageView<Role> {
        val companyId = roleService.getCompanyList(Role.ADMIN)[0]
        return getPage(connect.select()
                .from<Role>()
                .project("id", "user_id", "role_id", "company_id", "real_name", "department", "position")
                .matching(query.where().and("company_id").`is`(companyId))
                .page(query.page())
                .fetch()
                .all()
                , connect, query, "company_id = $companyId")
    }

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

    /**
     * 添加一个股东
     */
    suspend fun addStockholder(info: StockholderInfo): Role {
        val phone = info.phone ?: throw IllegalStateException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw  IllegalStateException("用户不存在 $phone")
        if (roleService.getCompanyList().contains(info.companyId)) {
            val roleId = roleService.getRoles().find { it.name == Role.USER }!!.id!!
            if (roleService.exists(user.id!!, info.companyId!!, roleId) == 0) {
                val stockId = stockService.findByCompanyId(info.companyId!!).first().id     // 添加公司的默认股票
                positionsDao.save(Positions(companyId = info.companyId, stockId = stockId, userId = user.id, amount = info.amount))
                val role = Role(userId = user.id,
                        roleId = roleId,
                        companyId = info.companyId,
                        realName = info.realName,
                        department = info.department,
                        position = info.position)
                return roleService.save(role)
            } else throw IllegalStateException("用户已经是股东 $phone")
        } else throw IllegalStateException("不能为公司 ${info.companyId} 添加股东，没有权限")
    }

    /**
     * 为公司添加一个管理员
     */
    suspend fun addCompanyAdmin(info: StockholderInfo): Role {  // TODO 一个公司只有一个管理员
        val phone = info.phone ?: throw IllegalStateException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw  IllegalStateException("用户不存在 $phone")
        if (roleService.getCompanyList().contains(info.companyId)) {// TODO 有问题，为公司添加管理员不能用roleService.getCompanyList()
            val userRoleId = roleService.getRoles().find { it.name == Role.USER }!!.id!!
            val adminRoleId = roleService.getRoles().find { it.name == Role.ADMIN }!!.id!!
            val role = roleService.find(user.id!!, info.companyId!!, userRoleId)
                    ?: throw IllegalStateException("用户不是股东")
            role.roleId = adminRoleId
            role.realName = info.realName
            return roleService.save(role)
        } else throw IllegalStateException("不能为公司 ${info.companyId} 添加股东，没有权限")
    }
}