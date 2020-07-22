package com.mt.mtuser.service.kline

import com.mt.mtuser.entity.Kline
import com.mt.mtuser.service.RedisUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/6/7
 */
abstract class ComputeKline {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    var nextKline: ComputeKline? = null

    @Autowired
    lateinit var klineService: KlineService

    @Autowired
    lateinit var redisUtil: RedisUtil
    abstract val tableName: String

    /**
     * 判断是否该计算k线
     */
    abstract fun handlerRequest(time: LocalDateTime): Boolean

    /**
     * 格式化时间，格式化为整点
     */
    abstract fun formatDate(time: LocalDateTime): LocalDateTime

    /**
     * 计算k线
     */
    abstract suspend fun compute(stockId: Int, companyId: Int, time: LocalDateTime, offset: Long): Kline
    abstract suspend fun getMinComputeTime(): LocalDateTime?
    abstract suspend fun isExist(time: LocalDateTime, stockId: Int, companyId: Int): Boolean
    abstract fun step(): Long

    /**
     * 设置责任链的下一个，一定要根据时间长短来设置先后顺序，
     * 按时间升序链接，如1分钟k线在15分钟k线之前
     */
    fun <T : ComputeKline> setNext(t: T): T {
        this.nextKline = t
        logger.info("{} -> {}", this.tableName, t.tableName)
        return t
    }

    /**
     * 执行下一个责任链
     * @param offset 偏移量，计算k线时时间是向后取的，
     * 如计算4小时的k线12号 20：00：00到13号 00：00：00时插入的k线数据的时间点是13号00：00：00
     * 这时计算日k时就不能取13号 00：00：00的4小时k线，因为这个点时12号的数据，所以应该偏移4小时取13号 04：00：00之后的4小时k线
     */
    suspend fun next(stockId: Int, companyId: Int, offset: Long) {
        nextKline?.handler(stockId, companyId, offset)
    }

    suspend fun handler(stockId: Int, companyId: Int, offset: Long = 0) {
        val lastTime = redisUtil.getKlineLastTime(tableName, stockId)
                ?: klineService.getLastTimeByKline(tableName) ?: getMinComputeTime()
        if (lastTime != null) {
            val formatTime = formatDate(lastTime)
            val now = LocalDateTime.now()
            var time = formatTime
            while (time.isBefore(now) && handlerRequest(time)) {
                val kline = compute(stockId, companyId, time, offset)
                if (!kline.isEmpty()) {
                    if (isExist(kline.time!!, stockId, companyId)) {
                        klineService.updateKline(kline, tableName)
                    } else {
                        klineService.saveKline(kline, tableName)
                    }
                }
                redisUtil.setKlineLastTime(tableName, stockId, kline.time!!)
                logger.info("k线计算 {} {} {} {} {}", stockId, companyId, tableName, time, offset)
                time = time.plusSeconds(step())
            }
        }
        next(stockId, companyId, step())
    }

}