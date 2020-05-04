package com.mt.mtengine.service

import com.mt.mtengine.dao.TradeInfoDao
import com.mt.mtengine.entity.MtTradeInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/5/2.
 */
@Service
class TradeInfoService {
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    fun save(tradeInfo: MtTradeInfo) = tradeInfoDao.save(tradeInfo)
}