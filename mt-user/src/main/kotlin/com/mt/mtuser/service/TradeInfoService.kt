package com.mt.mtuser.service

import com.mt.mtuser.dao.TradeInfoDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class TradeInfoService  {
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao


}