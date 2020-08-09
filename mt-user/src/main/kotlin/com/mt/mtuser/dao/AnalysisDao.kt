package com.mt.mtuser.dao

import com.mt.mtuser.entity.Analysis
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/7/19
 */
interface AnalysisDao: CoroutineCrudRepository<Analysis, Int> {

    @Query("select count(*) from mt_analysis where company_id = :companyId")
    suspend fun countByCompanyId(companyInt: Int): Long
}