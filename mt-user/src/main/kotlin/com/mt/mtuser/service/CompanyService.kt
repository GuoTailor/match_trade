package com.mt.mtuser.service

import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.PositionsDao
import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    @Autowired
    private lateinit var fileService: FileService

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
    suspend fun registerCompany(company: Company): Company {
        val newCompany = companyDao.save(company)
        company.brief?.let { fileService.addCompanyInfo(it, newCompany.id!!).awaitSingle() }
        val stock = Stock(companyId = newCompany.id, name = newCompany.name)
        stockService.save(stock)
        company.analystId?.let {
            addCompanyAnalyst(it, newCompany.id!!)
        }
        return newCompany
    }

    /**
     * 删除公司
     */
    suspend fun deleteById(id: Int) {
        fileService.deleteCompanyInfo(id)
        return companyDao.deleteById(id)
    }

    /**
     * 更新公司信息
     */
    suspend fun update(company: Company): Company {
        // TODO 判断公司房间模式
        company.brief?.let { fileService.addCompanyInfo(it, company.id!!).awaitSingle() }
        company.analystId?.let { addCompanyAnalyst(it, company.id!!) }
        return companyDao.save(company)
    }

    /**
     * 获取一个公司信息
     */
    suspend fun findCompany(id: Int): Company? {
        val company = companyDao.findById(id)
        company?.let { it.brief = fileService.getCompanyInfo(it.id!!).awaitSingle() }
        return company
    }

    /**
     * 获取自己加入的公司信息
     */
    suspend fun findCompany(query: PageQuery): PageView<Company> {
        val roles = roleService.getCompanyList()
        if (roles.isEmpty()) {
            return PageView()
        }
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val result = connect.select()
                .from<Company>()
                .matching(query.where().and("id").`in`(roles))
                .page(query.page())
                .fetch()
                .all()
                .flatMap { company ->
                    val stock = mono { positionsDao.countStockByCompanyIdAndUserId(userId, company.id!!) }
                    val money = mono { roleService.findByUserIdAndCompanyId(userId, company.id!!) }
                    val brief = fileService.getCompanyInfo(company.id!!)
                    stock.zipWith(money) { s, m ->
                        company.money = m?.money
                        company.stock = s
                        company
                    }.zipWith(brief) { c, b ->
                        c.brief = b
                        c
                    }
                }
        return getPage(result, connect, query, query.where().and("id").`in`(roles))
    }

    /**
     * 查找所有股东
     */
    suspend fun getAllShareholder(query: PageQuery): PageView<StockholderInfo> {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val where = query.where().and("s.company_id").`is`(companyId)
        return getPage(connect.execute(
                "select s.id, s.user_id, s.company_id, s.real_name, s.department, s.position, s.money, sum(p.amount) as amount " +
                        " from mt_stockholder s" +
                        " LEFT JOIN mt_positions p on p.company_id = s.company_id and p.user_id = s.user_id " +
                        " where $where group by s.id" +
                        query.toPageSql())
                .`as`(StockholderInfo::class.java)
                .fetch()
                .all()
                , connect, query, "mt_stockholder s", where)
    }

    /**
     * 查找所有公司
     */
    suspend fun findAllByQuery(query: PageQuery): PageView<Company> {
        val where = query.where("mc")
        return getPage(connect.execute("select mc.*, mu.phone, mu.nick_name, mu.id as analystId " +
                " from mt_company mc " +
                " LEFT JOIN mt_stockholder ms on ms.company_id = mc.id and ms.role_id = 2 " +
                " LEFT JOIN mt_user mu on ms.user_id = mu.id" +
                " where $where " + query.toPageSql())
                .map { r, _ ->
                    val company = Company()
                    company.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    company.name = r.get("name", String::class.java)
                    company.roomCount = r.get("room_count", java.lang.Integer::class.java)?.toInt()
                    company.mode = r.get("mode", String::class.java)
                    company.createTime = r.get("create_time", LocalDateTime::class.java)
                    company.licenseUrl = r.get("license_url", String::class.java)
                    company.creditUnionCode = r.get("credit_union_code", String::class.java)
                    company.legalPerson = r.get("legal_person", String::class.java)
                    company.unitAddress = r.get("unit_address", String::class.java)
                    company.unitContactName = r.get("unit_contact_name", String::class.java)
                    company.unitContactPhone = r.get("unit_contact_phone", String::class.java)
                    company.analystName = r.get("nick_name", String::class.java)
                    company.analystId = r.get("analystId", java.lang.Integer::class.java)?.toInt()
                    company.analystPhone = r.get("phone", String::class.java)
                    company
                }.all()
                , connect, query, "mt_company")
    }

    /**
     * 添加一个股东
     */
    suspend fun addStockholder(info: StockholderInfo): Stockholder {
        val phone = info.phone ?: throw IllegalStateException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw IllegalStateException("用户不存在 $phone")
        if (roleService.getCompanyList().contains(info.companyId)) {
            val roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
            val stockholder = roleService.findByUserIdAndRoleId(user.id!!, roleId)
                    ?: roleService.save(Stockholder(userId = user.id!!, roleId = roleId))
            if (stockholder.companyId != info.companyId) {
                val stockId = stockService.findByCompanyId(info.companyId!!).first().id     // 添加公司的默认股票
                positionsDao.save(Positions(companyId = info.companyId, stockId = stockId, userId = user.id, amount = info.amount))
                info.toStockholder(stockholder)
                stockholder.userId = user.id
                stockholder.roleId = roleId
                stockholder.companyId = info.companyId
                r2dbcService.dynamicUpdate(stockholder)
                        .matching(where("id").`is`(stockholder.id!!))
                        .fetch().awaitRowsUpdated()
                return stockholder
            } else throw IllegalStateException("用户已经是股东 ${stockholder.realName}")
        } else throw IllegalStateException("不能为公司 ${info.companyId} 添加股东，没有权限")
    }

    suspend fun deleteStockholder(id: Int) {
        val stockholder = roleService.findById(id) ?: error("错误，股东不存在")
        if (roleService.getCompanyList().contains(stockholder.companyId)) {
            val role = roleService.getRoles().find { it.name == Stockholder.USER }
            if (role!!.id == stockholder.roleId) {
                val stockId = stockService.findByCompanyId(stockholder.companyId!!).first().id!!
                val positions = positionsDao.findStockByCompanyIdAndUserIdAndStockId(stockholder.userId!!, stockholder.companyId!!, stockId)
                stockholder.cleanCompany()
                roleService.update(stockholder)
                if (positions != null) {
                    positionsDao.deleteById(positions.id!!)
                }
            } else error("不可删除角色 ${role.nameZh}")
        } else error("不能删除非本公司的股东，没有权限")
    }

    /**
     * 更新股东信息
     */
    suspend fun updateStockholder(info: StockholderInfo): Boolean {
        val stockholder = roleService.findById(info.id!!) ?: throw IllegalStateException("股东不存在")
        info.toStockholder(stockholder)
        val stockId = stockService.findByCompanyId(info.companyId!!).first().id     // 添加公司的默认股票
        val position = positionsDao.findStockByCompanyIdAndUserIdAndStockId(stockholder.userId!!, stockholder.companyId!!, stockId!!)
        if (info.amount != null) {
            position!!.amount = info.amount
        }
        positionsDao.save(position!!)
        return r2dbcService.dynamicUpdate(stockholder)
                .matching(where("id").`is`(stockholder.id!!))
                .fetch().awaitRowsUpdated() > 0
    }

    /**
     * 为公司添加一个管理员
     */
    suspend fun addCompanyAdmin(info: StockholderInfo): Stockholder {  // TODO 一个公司只有一个管理员
        val phone = info.phone ?: throw IllegalStateException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw  IllegalStateException("用户不存在 $phone")
        val userRoleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
        val adminRoleId = roleService.getRoles().find { it.name == Stockholder.ADMIN }!!.id!!
        val role = roleService.find(user.id!!, info.companyId!!, userRoleId)
                ?: throw IllegalStateException("用户不是股东")
        role.roleId = adminRoleId
        role.realName = info.realName
        return roleService.save(role)
    }

    /**
     * 为公司添加一个分析员
     */
    suspend fun addCompanyAnalyst(userId: Int, companyId: Int) {
        val user = userDao.findById(userId) ?: error("用户不存在 $userId")
        val userRoleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        val stockholder = roleService.find(userId, userRoleId) ?: error("用户：${user.phone}不是分析员")
        if (roleService.exists(userRoleId, companyId) == 0) {
            stockholder.id = null
            stockholder.companyId = companyId
            roleService.save(stockholder)
        } else error("该公司：$companyId 已有分析员")
    }

    /**
     * 获取交易概述
     */
    // TODO 想办法加缓存
    suspend fun getOverview(companyId: Int): Map<String, Overview> {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        return mapOf(
                "day" to tradeInfoService.dayOverview(userId, companyId),
                "month" to tradeInfoService.monthOverview(userId, companyId)
        )
    }
}