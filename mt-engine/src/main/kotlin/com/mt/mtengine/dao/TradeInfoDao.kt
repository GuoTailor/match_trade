package com.mt.mtengine.dao

import com.mt.mtcommon.TradeInfo
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : ReactiveCrudRepository<TradeInfo, Int> {
}