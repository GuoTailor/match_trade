package com.mt.mtuser.dao

import com.mt.mtuser.entity.Role
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/17.
 */
interface UserRoleDao: CoroutineCrudRepository<Role, Int> {
    @Query("select" +
            " ur.id, userid, roleid, companyid, name, name_zh" +
            " from mt_user_role ur" +
            " left join mt_role r on ur.roleid = r.id" +
            " where userid = $1")
    suspend fun selectRolesByUserId(userId: Int): Role

    @Query("insert into mt_user_role(userid, roleid, companyid) values(:userid, :roleid, :companyid)")
    suspend fun save(userId: Int, roleId: Int, companyId: Int): Int
}