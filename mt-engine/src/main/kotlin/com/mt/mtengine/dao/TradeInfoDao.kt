package com.mt.mtengine.dao

import com.mt.mtengine.entity.MtTradeInfo
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : ReactiveCrudRepository<MtTradeInfo, Int> {
}