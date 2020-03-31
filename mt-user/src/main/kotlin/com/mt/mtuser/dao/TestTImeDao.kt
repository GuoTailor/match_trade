package com.mt.mtuser.dao

import com.mt.mtuser.entity.TestTime
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/31.
 */
interface TestTImeDao : CoroutineCrudRepository<TestTime, Int>