package com.mt.mtuser.service

import com.mt.mtuser.dao.RoleDao
import com.mt.mtuser.dao.StockholderDao
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.Stockholder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.flow
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class RoleService {

    @Autowired
    private lateinit var stockholderDao: StockholderDao

    @Autowired
    private lateinit var roleDao: RoleDao

    @Autowired
    protected lateinit var connect: DatabaseClient
    private var roles: List<MtRole>? = null

    suspend fun getRoles(): List<MtRole> {
        return roles ?: findAll().toList()
    }

    fun selectRolesByUserId(userId: Int): Flow<Stockholder> {
        return connect.execute("select" +
                " ur.id, ur.real_name, ur.dp_id, ur.money, user_id, role_id, company_id, name, name_zh" +
                " from mt_stockholder ur" +
                " left join mt_role r on ur.role_id = r.id" +
                " where user_id = :userId")
                .bind("userId", userId)
                .map { r, _ ->
                    val s = Stockholder()
                    s.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                    s.userId = r.get("user_id", java.lang.Integer::class.java)?.toInt()
                    s.roleId = r.get("role_id", java.lang.Integer::class.java)?.toInt()
                    s.companyId = r.get("company_id", java.lang.Integer::class.java)?.toInt()
                    s.realName = r.get("real_name", String::class.java)
                    s.dpId = r.get("dp_id", java.lang.Integer::class.java)?.toInt()
                    s.money = r.get("money", BigDecimal::class.java)
                    s.name = r.get("name", String::class.java)
                    s.nameZh = r.get("name_zh", String::class.java)
                    s
                }.flow()
    }

    suspend fun getCompanyList(role: String? = null): List<Int> {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        val roles = selectRolesByUserId(userId)
        val companyList = mutableListOf<Int>()
        roles.collect {
            if (role == null || it.name == role) {  // role 为空就添加全部，role不为空就添加指定角色名的公司id
                it.companyId?.let { id -> companyList.add(id) }
            }
        }
        return companyList
    }

    // TODO 更新数据库表时要更新本地缓存
    suspend fun findAll(): Flow<MtRole> {
        val roles = roleDao.findAll()
        this.roles = roles.toList()
        return roles
    }

    suspend fun save(stockholder: Stockholder) = stockholderDao.save(stockholder)

    suspend fun update(sh: Stockholder) = stockholderDao.update(sh.id!!, sh.userId, sh.roleId, sh.companyId, sh.realName,
            sh.dpId, sh.money ?: BigDecimal(0))

    suspend fun exists(userId: Int, roleId: Int, companyId: Int) = stockholderDao.exists(userId, roleId, companyId)

    suspend fun exists(roleId: Int, companyId: Int) = stockholderDao.exists(roleId, companyId)

    suspend fun existsByCompanyId(companyId: Int) = stockholderDao.existsByCompanyId(companyId)

    suspend fun existsByDpId(dpId: Int) = stockholderDao.existsByDpId(dpId)

    suspend fun find(userId: Int, roleId: Int, companyId: Int) = stockholderDao.find(userId, roleId, companyId)

    suspend fun findById(id: Int) = stockholderDao.findById(id)

    suspend fun countByCompanyIdAndRoleId(companyId: Int, roleId: Int) = stockholderDao.countByCompanyIdAndRoleId(companyId, roleId)

    fun findByCompanyId(companyId: Int) = stockholderDao.findByCompanyId(companyId)

    suspend fun findByUserIdAndCompanyId(userId: Int, companyId: Int) = stockholderDao.findByUserIdAndCompanyId(userId, companyId)

    suspend fun findByUserIdAndRoleId(userId: Int, roleId: Int) = stockholderDao.findByUserIdAndRoleId(userId, roleId)

    suspend fun findByRoleIdAndCompanyId(roleId: Int, companyId: Int) = stockholderDao.findByRoleIdAndCompanyId(roleId, companyId)

    suspend fun findNoBinding(userId: Int, roleId: Int) = stockholderDao.findNoBinding(userId, roleId)

    suspend fun deleteById(id: Int) = stockholderDao.deleteById(id)

    suspend fun deleteByRoleIdAndCompanyId(roleId: Int, companyId: Int): Int = stockholderDao.deleteByRoleIdAndCompanyId(roleId, companyId)

}