package com.mt.mtuser.dao

import com.mt.mtuser.entity.StockholderInfo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/4/23.
 */
interface StockholderInfoDao: CoroutineCrudRepository<StockholderInfo, Int> {


    companion object {
        private const val sql = "id,company_id,stock_id,user_id,amount,real_name,department,position"
    }
}