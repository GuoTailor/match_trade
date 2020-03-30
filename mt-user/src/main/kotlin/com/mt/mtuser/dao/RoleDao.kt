package com.mt.mtuser.dao

import com.mt.mtuser.dao.entity.MtRole
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/18.
 */
interface RoleDao : CoroutineCrudRepository<MtRole, Int> {
}
