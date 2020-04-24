package com.mt.mtuser.service

import com.mt.mtuser.dao.RoleDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.Role
import kotlinx.coroutines.flow.Flow
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

    suspend fun selectRolesByUserId(userId: Int): Role? {
        return userRoleDao.selectRolesByUserId(userId)
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

}