package com.mt.mtuser.service

import com.mt.mtcommon.exception.BusinessException
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.PositionsDao
import com.mt.mtuser.dao.UserDao
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.department.DepartmentPostInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.EntityRowMapper
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Service
import java.math.BigDecimal
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

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var departmentPostService: DepartmentPostService

    @Autowired
    private lateinit var template: R2dbcEntityTemplate

    suspend fun count(): Long {
        return companyDao.count()
    }

    /**
     * 创建公司，顺便创建一支和公司名同名的股票
     * 顺便创建一
     */
    suspend fun registerCompany(company: Company): Company {
        val newCompany = companyDao.save(company)
        if (company.adminPhone != null) {
            val stockholderInfo =
                StockholderInfo(companyId = newCompany.id, phone = company.adminPhone, realName = company.adminName)
            addCompanyAdmin(stockholderInfo)
        }
        company.brief?.let { fileService.addCompanyInfo(it, newCompany.id!!).awaitSingle() }
        val stock = Stock(companyId = newCompany.id, name = newCompany.name)
        stockService.save(stock)
        company.analystId?.let {
            addCompanyAnalyst(it, newCompany.id!!)
        }
        departmentPostService.bindDepartment(DepartmentPostInfo(null, "股东", "股东", newCompany.id))
        return newCompany
    }

    /**
     * 删除公司
     */
    suspend fun deleteById(id: Int) {
        if (roleService.existsByCompanyId(id) > 0) error("公司下存在员工，无法删除")
        val roleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        val adminRoleId = roleService.getRoles().find { it.name == Stockholder.ADMIN }!!.id!!
        val userRoleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
        roleService.deleteByRoleIdAndCompanyId(roleId, id)
        val adminUser = roleService.findByRoleIdAndCompanyId(adminRoleId, id)
        if (adminUser != null) {
            adminUser.companyId = null
            adminUser.dpId = null
            adminUser.money = BigDecimal.ZERO
            adminUser.realName = null
            adminUser.roleId = userRoleId
            roleService.update(adminUser)
        }
        roomService.getRoomByCompanyId(listOf(id)).forEach {
            if (it.isEnable()) {
                try {
                    roomService.enableRoom(it.roomId!!, false, it.flag)
                } catch (e: Throwable) {
                    logger.info("房间关闭失败 {} {} ", it.roomId, it.flag)
                }
            }
            roomService.deleteRoom(it.roomId!!, it.flag)
        }
        fileService.deleteCompanyInfo(id)
        return companyDao.deleteById(id)
    }

    /**
     * 更新公司信息
     */
    suspend fun update(company: Company): Company {
        companyDao.findById(company.id!!) ?: throw BusinessException("公司不存在")
        // TODO 判断公司房间模式
        company.brief?.let { fileService.addCompanyInfo(it, company.id!!).awaitSingle() }
        company.analystId?.let { addCompanyAnalyst(it, company.id!!) }
        company.enable = company.enable ?: "1"
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
        val result = template.select<Company>()
            .matching(Query.query(query.where().and("id").`in`(roles)).with(query.page()))
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
        return getPage(result, template, query, query.where().and("id").`in`(roles))
    }

    /**
     * 查找所有股东
     */
    suspend fun getAllShareholder(query: PageQuery, id: Int?): PageView<StockholderInfo> {
        val role = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
        var where = query.where("s").and("s.role_id").`is`(role)
        val superAdmin = BaseUser.getcurrentUser().awaitSingle().roles.find { it.authority == Stockholder.SUPER_ADMIN }
        if (superAdmin != null && id != null) {
            where = where.and("s.company_id").`is`(id)
        } else if (superAdmin == null) {
            val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
            where = where.and("s.company_id").`is`(companyId)
        }
        return getPage(
            template.databaseClient.sql(
                "select s.id, s.user_id, s.company_id, s.real_name, s.dp_id, s.money, p.amount, p.\"limit\", " +
                        " (select md.name as department from mt_department_post mdp LEFT JOIN mt_department md on md.id = mdp.department_id where mdp.id = s.dp_id)," +
                        " (select mp.name as position from mt_department_post mdp LEFT JOIN mt_post mp on mp.id = mdp.post_id where mdp.id = s.dp_id), " +
                        " (select mu.phone from mt_user mu where s.user_id = mu.id) " +
                        " from mt_stockholder s" +
                        " LEFT JOIN mt_positions p on p.company_id = s.company_id and p.user_id = s.user_id " +
                        " ${if (where.toString().isBlank()) "" else "where"} $where" +
                        query.toPageSql()
            ).map(EntityRowMapper(StockholderInfo::class.java, template.converter))
                .all(), template, query, "mt_stockholder s", where
        )
    }

    /**
     * 获取股东通过部门
     */
    suspend fun getShareholderByDepartment(query: PageQuery, companyId: Int, name: String): PageView<StockholderInfo> {
        val idList = template.databaseClient.sql(
            "select mdp.id from mt_department_post mdp, mt_department md " +
                    " where mdp.company_id = :companyId " +
                    "  and mdp.department_id = md.id " +
                    "  and md.name = :name"
        )
            .bind("companyId", companyId)
            .bind("name", name)
            .map(EntityRowMapper(StockholderInfo::class.java, template.converter))
            .all().collectList().awaitSingle()
        if (idList.isEmpty()) error("公司不存在部门")
        val role = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
        val where = query.where("s")
            .and("s.company_id").`is`(companyId)
            .and("s.dp_id").`in`(idList)
            .and("s.role_id").`is`(role)
        return getPage(
            template.databaseClient.sql(
                "select s.id, s.user_id, s.company_id, s.real_name, s.dp_id, s.money, p.amount, p.\"limit\", " +
                        " (select md.name as department from mt_department_post mdp LEFT JOIN mt_department md on md.id = mdp.department_id where mdp.id = s.dp_id)," +
                        " (select mp.name as position from mt_department_post mdp LEFT JOIN mt_post mp on mp.id = mdp.post_id where mdp.id = s.dp_id), " +
                        " (select mu.phone from mt_user mu where s.user_id = mu.id) " +
                        " from mt_stockholder s" +
                        " LEFT JOIN mt_positions p on p.company_id = s.company_id and p.user_id = s.user_id " +
                        " where $where " +
                        query.toPageSql()
            ).map(EntityRowMapper(StockholderInfo::class.java, template.converter))
                .all(), template, query, "mt_stockholder s", where
        )
    }

    /**
     * 查找所有公司
     */
    suspend fun findAllByQuery(query: PageQuery): PageView<Company> {
        val where = query.where("mc")
        return getPage(
            template.databaseClient.sql(
                "select mc.*, mu.phone, mu.nick_name, mu.id as analystId" +
                        " , mu2.phone as adminPhone, ms2.real_name as adminName" +
                        " from mt_company mc " +
                        " LEFT JOIN mt_stockholder ms on ms.company_id = mc.id and ms.role_id = 2 " +
                        " LEFT JOIN mt_user mu on ms.user_id = mu.id" +
                        " LEFT JOIN mt_stockholder ms2 on ms2.company_id = mc.id and ms2.role_id = 3" +
                        " LEFT JOIN mt_user mu2 on ms2.user_id = mu2.id" +
                        " where $where " + query.toPageSql()
            ).map { r, _ ->
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
                company.enable = r.get("enable", String::class.java)
                company.analystName = r.get("nick_name", String::class.java)
                company.analystId = r.get("analystId", java.lang.Integer::class.java)?.toInt()
                company.analystPhone = r.get("phone", String::class.java)
                company.adminPhone = r.get("adminPhone", String::class.java)
                company.adminName = r.get("adminName", String::class.java)
                company
            }.all(), template, query, "mt_company"
        )
    }

    /**
     * 分析员获取自己管理的公司
     */
    suspend fun findByUser(query: PageQuery): PageView<Company> {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val where = query.where("mc")
        return getPage(
            template.databaseClient.sql(
                "select mc.*, mu.phone, mu.nick_name, mu.id as analystId " +
                        " from mt_company mc, " +
                        " mt_stockholder ms " +
                        " LEFT JOIN mt_user mu on ms.user_id = mu.id " +
                        " where mc.id = ms.company_id " +
                        "  and ms.role_id = 2 " +
                        "  and ms.user_id = :userId" +
                        "  ${if (where.isEmpty) "" else " and $where"} " + query.toPageSql()
            ).bind("userId", userId)
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
                    company.enable = r.get("enable", String::class.java)
                    company.analystName = r.get("nick_name", String::class.java)
                    company.analystId = r.get("analystId", java.lang.Integer::class.java)?.toInt()
                    company.analystPhone = r.get("phone", String::class.java)
                    company
                }.all(), template, query, "mt_company"
        )
    }

    /**
     * 添加一个股东
     */
    suspend fun addStockholder(info: StockholderInfo): Stockholder {
        val phone = info.phone ?: throw BusinessException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw BusinessException("用户不存在 $phone")
        if (roleService.getCompanyList().contains(info.companyId)) {
            val roleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
            val stockholder = roleService.findByUserIdAndRoleId(user.id!!, roleId)
                ?: roleService.save(Stockholder(userId = user.id!!, roleId = roleId))
            if (stockholder.companyId != info.companyId) {
                if (stockholder.companyId != null) error("用户以绑定其他公司")
                val stockId = stockService.findByCompanyId(info.companyId!!).first().id     // 添加公司的默认股票
                positionsDao.save(
                    Positions(
                        companyId = info.companyId,
                        stockId = stockId,
                        userId = user.id,
                        amount = info.amount
                    )
                )
                info.toStockholder(stockholder)
                stockholder.userId = user.id
                stockholder.roleId = roleId
                stockholder.companyId = info.companyId
                r2dbcService.dynamicUpdate(stockholder)
                    .awaitSingle()
                return stockholder
            } else throw BusinessException("用户已经是股东 ${stockholder.realName}")
        } else throw BusinessException("不能为公司 ${info.companyId} 添加股东，没有权限")
    }

    suspend fun deleteStockholder(id: Int) {
        val stockholder = roleService.findById(id) ?: error("错误，股东不存在")
        if (roleService.getCompanyList().contains(stockholder.companyId)) {
            val role = roleService.getRoles().find { it.name == Stockholder.USER }
            if (role!!.id == stockholder.roleId) {
                val stockId = stockService.findByCompanyId(stockholder.companyId!!).first().id!!
                val positions = positionsDao.findStockByCompanyIdAndUserIdAndStockId(
                    stockholder.userId!!,
                    stockholder.companyId!!,
                    stockId
                )
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
        val stockholder = roleService.findById(info.id!!) ?: throw BusinessException("股东不存在")
        info.toStockholder(stockholder)
        val stockId = stockService.findByCompanyId(info.companyId!!).first().id     // 添加公司的默认股票
        val position = positionsDao.findStockByCompanyIdAndUserIdAndStockId(
            stockholder.userId!!,
            stockholder.companyId!!,
            stockId!!
        )
        if (info.amount != null) {
            position!!.amount = info.amount
        }
        positionsDao.update(position!!)
        if (info.limit != null) {
            positionsDao.updateLimit(stockholder.userId!!, stockholder.companyId!!, info.limit)
        }
        return r2dbcService.dynamicUpdate(stockholder)
            .awaitSingle() > 0
    }

    /**
     * 为公司绑定一个管理员
     * 一个公司只有一个管理员
     */
    suspend fun addCompanyAdmin(info: StockholderInfo): Stockholder {
        val phone = info.phone ?: throw BusinessException("手机号不能为空")
        val user = userDao.findByPhone(phone) ?: throw BusinessException("用户不存在 $phone")
        val userRoleId = roleService.getRoles().find { it.name == Stockholder.USER }!!.id!!
        val adminRoleId = roleService.getRoles().find { it.name == Stockholder.ADMIN }!!.id!!
        val role = roleService.findNoBinding(user.id!!, userRoleId) ?: error("用户:$phone 不存在或已是股东")
        val currentAdmin = roleService.findByRoleIdAndCompanyId(adminRoleId, info.companyId!!)
        return if (currentAdmin == null) {
            role.roleId = adminRoleId
            role.realName = info.realName
            role.companyId = info.companyId
            roleService.save(role)
        } else {
            currentAdmin.roleId = userRoleId
            role.roleId = adminRoleId
            role.realName = info.realName
            roleService.save(currentAdmin)
            roleService.save(role)
        }
    }

    /**
     * 为公司绑定一个分析员
     * 一个公司只有一个分析员
     */
    suspend fun addCompanyAnalyst(userId: Int, companyId: Int) {
        val user = userDao.findById(userId) ?: error("用户不存在 $userId")
        val roleId = roleService.getRoles().find { it.name == Stockholder.ANALYST }!!.id!!
        val analyst = roleService.findByUserIdAndRoleId(userId, roleId) ?: error("用户：${user.phone}不是分析员")
        val currentAnalyst = roleService.findByRoleIdAndCompanyId(roleId, companyId)
        if (currentAnalyst == null) {
            analyst.id = null
            analyst.companyId = companyId
            roleService.save(analyst)
        } else {
            currentAnalyst.userId = analyst.userId
            roleService.save(currentAnalyst)
        }
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

    /**
     * 修改交易限制
     */
    suspend fun updateLimit(userId: List<Int>, limit: Int, companyId: Int): Int {
        return positionsDao.updateLimit(userId, companyId, limit)
    }

    /**
     * 使能一个公司
     */
    suspend fun updateEnable(id: Int, enable: String): Int {
        return companyDao.enable(id, enable)
    }

}
