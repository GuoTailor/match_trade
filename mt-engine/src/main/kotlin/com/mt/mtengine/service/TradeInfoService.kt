package com.mt.mtengine.service

import com.mt.mtcommon.TradeInfo
import com.mt.mtengine.dao.TradeInfoDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/5/2.
 */
@Service
class TradeInfoService {
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    fun save(tradeInfo: TradeInfo) = tradeInfoDao.save(tradeInfo)
}