package com.mt.mtuser.dao

import com.mt.mtuser.entity.Stockholder
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

/**
 * Created by gyh on 2020/3/17.
 */
interface StockholderDao : CoroutineCrudRepository<Stockholder, Int> {

    @Modifying
    @Query("insert into mt_stockholder(user_id, role_id, company_id) values(:userId, :roleId, :companyId)")
    suspend fun save(userId: Int, roleId: Int, companyId: Int?): Int

    @Modifying
    @Query("UPDATE mt_stockholder SET user_id = :userId, role_id = :roleId, company_id = :companyId," +
            " real_name = :realName, dp_id =:dpId, money = :money" +
            " WHERE mt_stockholder.id = :id ")
    suspend fun update(id: Int, userId: Int?, roleId: Int?, companyId: Int?, realName: String?, dpId: Int?, money: BigDecimal): Int

    @Query("select count(*) from mt_stockholder where company_id = :companyId and role_id = :roleId")
    suspend fun countByCompanyIdAndRoleId(companyId: Int, roleId: Int): Long

    @Query("select count(*) from mt_stockholder where user_id = :userId and role_id = :roleId and company_id = :companyId limit 1")
    suspend fun exists(userId: Int, roleId: Int, companyId: Int): Int

    @Query("select count(*) from mt_stockholder where company_id = :companyId and role_id = :roleId limit 1")
    suspend fun exists(roleId: Int, companyId: Int): Int

    @Query("select count(*) from mt_stockholder where company_id = :companyId and role_id != 2 and role_id != 3 limit 1")
    suspend fun existsByCompanyId(companyId: Int): Int

    @Query("select count(*) from mt_stockholder where dp_id = :dpId")
    suspend fun existsByDpId(dpId: Int): Int

    @Query("select * from mt_stockholder where user_id = :userId and role_id = :roleId and company_id = :companyId")
    suspend fun find(userId: Int, roleId: Int, companyId: Int): Stockholder?

    @Query("select * from mt_stockholder where user_id = :userId and company_id = :companyId")
    suspend fun findByUserIdAndCompanyId(userId: Int, companyId: Int): Stockholder?

    @Query("select * from mt_stockholder where user_id = :userId and role_id = :roleId limit 1")
    suspend fun findByUserIdAndRoleId(userId: Int, roleId: Int): Stockholder?

    @Query("select * from mt_stockholder where role_id = :roleId and company_id = :companyId limit 1")
    suspend fun findByRoleIdAndCompanyId(roleId: Int, companyId: Int): Stockholder?

    @Query("select * from mt_stockholder where user_id = :userId and role_id = :roleId and company_id is null limit 1")
    suspend fun findNoBinding(userId: Int, roleId: Int): Stockholder?

    @Query("select * from mt_stockholder where company_id = :companyId")
    fun findByCompanyId(companyId: Int): Flow<Stockholder>

    @Modifying
    @Query("delete from mt_stockholder where role_id = :roleId and company_id = :companyId")
    suspend fun deleteByRoleIdAndCompanyId(roleId: Int, companyId: Int): Int
}
