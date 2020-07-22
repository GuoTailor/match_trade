package com.mt.mtuser.dao

import com.mt.mtuser.entity.Analysis
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/7/19
 */
interface AnalysisDao: CoroutineCrudRepository<Analysis, Int> {
}