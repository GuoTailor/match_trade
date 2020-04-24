package com.mt.mtuser.dao

import com.mt.mtuser.entity.Positions
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/4/23.
 */
interface PositionsDao : CoroutineCrudRepository<Positions, Int> {

    companion object {
        const val sql = "id,company_id,stock_id,user_id,amount"
    }
}