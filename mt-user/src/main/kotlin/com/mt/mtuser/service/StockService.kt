package com.mt.mtuser.service

import com.mt.mtuser.dao.StockDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/22.
 */
@Service
class StockService {
    @Autowired
    private lateinit var stockDao: StockDao

    suspend fun findById(id: Int) = stockDao.findById(id)
}