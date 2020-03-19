package com.mt.mtuser.service

import com.mt.mtuser.dao.RoleDao
import com.mt.mtuser.dao.UserRoleDao
import com.mt.mtuser.dao.entity.MtRole
import com.mt.mtuser.entity.Role
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

    fun selectRolesByUserId(userId: Int): Mono<Role> {
        return userRoleDao.selectRolesByUserId(userId)
    }

    fun findAll(): Flux<MtRole> {
        return roleDao.findAll()
    }
    
}