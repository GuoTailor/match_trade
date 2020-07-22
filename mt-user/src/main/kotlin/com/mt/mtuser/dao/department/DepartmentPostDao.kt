package com.mt.mtuser.dao.department

import com.mt.mtuser.entity.department.DepartmentPost
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/7/20
 */
interface DepartmentPostDao: CoroutineCrudRepository<DepartmentPost, Int> {

    fun findAllByCompanyId(companyId: Int): Flow<DepartmentPost>

}