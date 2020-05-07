package com.mt.mtuser.service

import com.mt.mtuser.dao.RoleDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.BaseUser
import com.mt.mtuser.entity.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/18.
 */
@Service
class RoleService {

    @Autowired
    private lateinit var userRoleDao: UserRoleDao

    @Autowired
    private lateinit var roleDao: RoleDao
    var roles: List<MtRole>? = null

    suspend fun getRoles(): List<MtRole> {
        return roles ?: findAll().toList()
    }

    fun selectRolesByUserId(userId: Int): Flow<Role> {
        return userRoleDao.selectRolesByUserId(userId)
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

    suspend fun save(role: Role) = userRoleDao.save(role)

    suspend fun exists(userId: Int, roleId: Int, companyId: Int) = userRoleDao.exists(userId, roleId, companyId)

    suspend fun find(userId: Int, roleId: Int, companyId : Int) = userRoleDao.find(userId, roleId, companyId)

    fun findByCompanyId(companyId: Int) = userRoleDao.findByCompanyId(companyId)
}