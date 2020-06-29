package com.mt.mtuser.service.kline

import com.mt.mtuser.entity.Kline
import com.mt.mtuser.service.RedisUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/6/7
 */
abstract class ComputeKline : ApplicationRunner, Comparable<ComputeKline> {
    @Autowired
    lateinit var klineService: KlineService

    @Autowired
    lateinit var redisUtil: RedisUtil
    abstract val tableName: String

    /**
     * 判断是否该计算k线
     */
    abstract fun handlerRequest(time: Long): Boolean

    /**
     * 格式化时间，格式化为整点
     */
    abstract fun formatDate(time: Long): Long

    /**
     * 计算k线
     */
    abstract suspend fun compute(stockId: Int, companyId: Int, time: Long): Kline
    abstract suspend fun getMinComputeTime(): LocalDateTime?
    abstract fun step(): Long

    suspend fun getLastTime(stockId: Int): Long? {
        return redisUtil.getKlineLastTime(tableName, stockId)
    }

    suspend fun handler(stockId: Int, companyId: Int, time: Long): Kline {
        val formatTime = formatDate(time)
        val kline = compute(stockId, companyId, formatTime)
        redisUtil.setKlineLastTime(tableName, stockId, formatTime)
        return kline
    }

    override fun run(args: ApplicationArguments) {
        klineService.register(this)
    }

    override fun compareTo(other: ComputeKline): Int {
        return step().compareTo(other.step())
    }
}