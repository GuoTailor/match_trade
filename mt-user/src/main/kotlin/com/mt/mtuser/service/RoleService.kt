package com.mt.mtuser.service

import com.mt.mtuser.dao.RoleDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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

    suspend fun selectRolesByUserId(userId: Int): Role {
        return userRoleDao.selectRolesByUserId(userId)
    }

    suspend fun findAll(): Flow<MtRole> {
        val roles = roleDao.findAll()
        this.roles = roles.toList()
        return roles
    }

    suspend fun save(role: Mono<Role>) = userRoleDao.save(role.awaitSingle())

}