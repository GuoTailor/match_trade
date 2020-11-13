package com.mt.mtuser.service

import com.mt.mtcommon.*
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Overview
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.http.HttpHeaders
import org.springframework.http.ZeroCopyHttpOutputMessage
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.File
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*


/**
 * Created by gyh on 2020/5/7.
 */
@Service
class TradeInfoService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var connect: DatabaseClient

    /**
     * 获取今天的交易量
     */
    suspend fun countStockByTradeTime(startTime: LocalDateTime = LocalTime.MIN.toLocalDateTime()) =
            tradeInfoDao.countStockByTradeTime(startTime, LocalDateTime.now())

    /**
     * 获取总的交易量
     */
    suspend fun countStock() = tradeInfoDao.countStock()

    /**
     * 获取公司今天的交易量
     */
    suspend fun countStockByTradeTimeAndCompanyId(startTime: LocalDateTime = LocalTime.MIN.toLocalDateTime()): Long {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        //val stockId = stockService.findByCompanyId(companyId).first()// TODO 替换为股票id
        return countStockByTradeTimeAndCompanyId(startTime, LocalDateTime.now(), companyId)
    }

    suspend fun countStockByTradeTimeAndCompanyId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int) =
            tradeInfoDao.countStockByTradeTimeAndCompanyId(startTime, endTime, companyId)

    /**
     * 获取今天交易额
     */
    suspend fun countMoneyByTradeTime(startTime: LocalDateTime = LocalTime.MIN.toLocalDateTime()) =
            tradeInfoDao.countMoneyTradeTime(startTime, LocalDateTime.now())

    /**
     * 获取总的交易额
     */
    suspend fun countMoney() = tradeInfoDao.countMoney()

    /**
     * 查询指定日期的活跃用户数
     */
    suspend fun countUserByTradeTime(time: LocalDateTime = LocalTime.MIN.toLocalDateTime()) =
            tradeInfoDao.countUserByTradeTime(time)

    /**
     * 获取公司今天的交易额
     */
    suspend fun countMoneyByTradeTimeAndCompanyId(startTime: LocalDateTime = LocalTime.MIN.toLocalDateTime()): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return countMoneyByTradeTimeAndCompanyId(startTime, LocalDateTime.now(), companyId)
    }

    suspend fun countMoneyByTradeTimeAndCompanyId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int) =
            tradeInfoDao.countMoneyByTradeTimeAndCompanyId(startTime, endTime, companyId)

    /**
     * 获取昨天的收盘价，也就是今天的开盘价
     */
    suspend fun getYesterdayClosingPriceByCompanyId(): BigDecimal {
        // TODO 交易失败的不计入计算
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        // 今天凌晨
        val endTime = LocalTime.MIN.toLocalDateTime()
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(endTime, companyId) ?: BigDecimal(0)
    }

    suspend fun getYesterdayClosingPriceByRoomId(roomId: String): BigDecimal {
        val startTime = LocalTime.MIN.toLocalDateTime().minusDays(1)
        val endTime = LocalTime.MIN.toLocalDateTime()
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime, endTime, roomId)
                ?: BigDecimal(0)
    }

    suspend fun getClosingPriceByRoomId(roomId: String, startTime: LocalDateTime, endTime: LocalDateTime): BigDecimal? {
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime, endTime, roomId)
    }

    suspend fun getOpenPriceByRoomId(startTime: LocalDateTime, endTime: LocalDateTime, roomId: String) =
            tradeInfoDao.findFirstPriceByTradeTimeAndRoomId(startTime, endTime, roomId) ?: BigDecimal(0)

    /**
     * 获取今天的最新一次报价
     */
    suspend fun getTodayOpeningPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(LocalDateTime.now(), companyId) ?: BigDecimal(0)
    }

    /**
     * 获取今天的平均报价
     */
    suspend fun getAvgPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTime = LocalDateTime.now()
        return tradeInfoDao.avgPriceByTradeTimeAndCompanyId(startTime, endTime, companyId)
    }

    suspend fun buyOverview(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int, buyerId: Int): Overview {
        return connect.execute("select COALESCE(sum(trade_amount), 0) as buyStock," +
                " COALESCE(sum(trade_money), 0) as buyMoney," +
                " COALESCE(avg(trade_price), 0) as avgBuyMoney " +
                " from ${TradeInfoDao.table} " +
                " where trade_time >= :startTime " +
                " and trade_time <= :endTime " +
                " and company_id = :companyId " +
                " and buyer_id = :buyerId")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .bind("buyerId", buyerId)
                .map { r, _ ->  // 我不知道为什么不能用 as方法进行转换
                    val buyStock = r.get("buyStock", java.lang.Long::class.java)
                    val buyMoney = r.get("buyMoney", BigDecimal::class.java)
                    val avgBuyMoney = r.get("avgBuyMoney", BigDecimal::class.java)
                    Overview(buyStock = buyStock!!.toLong(), buyMoney = buyMoney, avgBuyMoney = avgBuyMoney)
                }.one()
                .awaitSingle()
    }

    suspend fun sellOverview(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int, sellId: Int): Overview {
        return connect.execute("select COALESCE(sum(trade_amount), 0) as sellStock," +
                " COALESCE(sum(trade_money), 0) as sellMoney," +
                " COALESCE(avg(trade_price), 0) as avgSellMoney " +
                " from ${TradeInfoDao.table} " +
                " where trade_time >= :startTime " +
                " and trade_time <= :endTime " +
                " and company_id = :companyId " +
                " and seller_id = :sellId")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .bind("sellId", sellId)
                .map { r, _ ->
                    val sellStock = r.get("sellStock", java.lang.Long::class.java)
                    val sellMoney = r.get("sellMoney", BigDecimal::class.java)
                    val avgSellMoney = r.get("avgSellMoney", BigDecimal::class.java)
                    Overview(sellStock = sellStock!!.toLong(), sellMoney = sellMoney, avgSellMoney = avgSellMoney)
                }.one()
                .awaitSingle()
    }

    /**
     * 获取今日成交概述
     */
    suspend fun dayOverview(userId: Int, companyId: Int): Overview {
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTime = LocalDateTime.now()
        val buyOverview = buyOverview(startTime, endTime, companyId, userId)
        val sellOverview = sellOverview(startTime, endTime, companyId, userId)
        buyOverview.copyNotNullField(sellOverview)
        buyOverview.computeNetBuy()
        return buyOverview
    }

    /**
     * 获取本月成交概述
     */
    suspend fun monthOverview(userId: Int, companyId: Int): Overview {
        val startTime = firstDay()
        val endTime = lastDay()
        val buyOverview = buyOverview(startTime, endTime, companyId, userId)
        val sellOverview = sellOverview(startTime, endTime, companyId, userId)
        buyOverview.copyNotNullField(sellOverview)
        buyOverview.computeNetBuy()
        return buyOverview
    }

    /**
     * 获取指定时间范围的最大和最小报价
     */
    suspend fun getMaxMinPrice(roomId: String, startTime: LocalDateTime, endTime: LocalDateTime): Map<String, BigDecimal> {
        return findMaxMinPriceByTradeTimeAndRoomId(roomId, startTime, endTime)
    }

    /**
     * 获取订单详情
     */
    suspend fun findDetailsById(id: Int): TradeInfo? {
        return tradeInfoDao.findDetailsById(id)
    }

    /**
     * 查询指定时间内的历史订单
     */
    suspend fun findOrder(roomId: String, query: PageQuery, endTime: LocalDateTime): PageView<TradeInfo> {
        val where = query.where()
                .and("room_id").`is`(roomId)
                .and("trade_time").lessThan(endTime)
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

    /**
     * 查找指定房间的历史订单
     */
    suspend fun findOrder(roomId: String, query: PageQuery): PageView<TradeInfo> {
        val where = query.where().and("room_id").`is`(roomId)
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

    /**
     * 查找指定公司的历史订单
     */
    suspend fun findOrderByCompany(companyId: Int, date: LocalDate, query: PageQuery): PageView<TradeInfo> {
        val endTime = date.plusDays(1)
        val where = query.where().and("company_id").`is`(companyId).and("trade_time").between(date, endTime)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // 无赖之举，使用connect.execute无法使用matching，只能手动拼接字符串，就必须格式化时间，
        // 而使用connect.select格式化时间后会抱怨：操作符不存在: timestamp without time zone >= character varying
        val countWhere = query.where().and("company_id").`is`(companyId).and("trade_time").between("'${date.format(formatter)}'", "'${endTime.format(formatter)}'")
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, countWhere)
    }

    suspend fun findOrderByCompanyAndDpId(companyId: Int, date: LocalDate) {

    }

    /**
     * 查询指定用户的交易记录
     */
    suspend fun findOrderByUserId(userId: Int, query: PageQuery, isBuy: Boolean?, date: LocalDate): PageView<TradeInfo> {
        val endTime = date.plusDays(1)
        val where = when {
            isBuy == null -> query.where().and(where("buyer_id").`is`(userId).or("seller_id").`is`(userId))
            isBuy -> query.where().and(where("buyer_id").`is`(userId))
            else -> query.where().and(where("seller_id").`is`(userId))
        }.and(where("trade_time").between(date, endTime))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        // 无赖之举，使用connect.execute无法使用matching，只能手动拼接字符串，就必须格式化时间，
        // 而使用connect.select格式化时间后会抱怨：操作符不存在: timestamp without time zone >= character varying
        val countWhere = when {
            isBuy == null -> query.where().and(where("buyer_id").`is`(userId).or("seller_id").`is`(userId))
            isBuy -> query.where().and(where("buyer_id").`is`(userId))
            else -> query.where().and(where("seller_id").`is`(userId))
        }.and(where("trade_time").between("'${date.format(formatter)}'", "'${endTime.format(formatter)}'"))

        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, countWhere)
    }

    suspend fun findOrderByUserId(userId: Int, query: PageQuery, isBuy: Boolean?) : PageView<TradeInfo> {
        val where = when {
            isBuy == null -> query.where().and(where("buyer_id").`is`(userId).or("seller_id").`is`(userId))
            isBuy -> query.where().and(where("buyer_id").`is`(userId))
            else -> query.where().and(where("seller_id").`is`(userId))
        }
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

    /**
     * 按天统计交易详情
     */
    suspend fun statisticsOrderByDay(page: PageQuery, companyId: Int): PageView<Map<String, Any?>> {
        val stockId = stockService.findByCompanyId(companyId).first().id!!
        val where = page.where("k").and("k.stock_id").`is`(stockId)
        val pageSql = page.toPageSql()
        val sql = "select k.*, count(rr.id) as openNumber from mt_1d_kline k " +
                " left join mt_room_record rr " +
                " on rr.stock_id = k.stock_id " +
                " and rr.start_time between k.time and k.time + INTERVAL'1 day'  " +
                " where $where group by k.id $pageSql"
        return getPage(connect.execute(sql)
                .map { r, _ ->
                    mapOf("id" to r.get("id", java.lang.Long::class.java),
                            "stockId" to r.get("stock_id", java.lang.Integer::class.java),
                            "time" to r.get("time", LocalDateTime::class.java)?.toEpochMilli(),
                            "tradesCapacity" to r.get("trades_capacity", java.lang.Long::class.java),
                            "tradesVolume" to r.get("trades_volume", BigDecimal::class.java),
                            "tradesNumber" to r.get("trades_number", java.lang.Integer::class.java),
                            "avgPrice" to r.get("avg_price", BigDecimal::class.java),
                            "maxPrice" to r.get("max_price", BigDecimal::class.java),
                            "minPrice" to r.get("min_price", BigDecimal::class.java),
                            "openPrice" to r.get("open_price", BigDecimal::class.java),
                            "closePrice" to r.get("close_price", BigDecimal::class.java),
                            "companyId" to r.get("company_id", java.lang.Integer::class.java),
                            "openNumber" to r.get("openNumber", java.lang.Integer::class.java))
                }.all(), connect, page, "mt_1d_kline k", where)
    }

    /**
     * 按部门统计交易详情
     */
    suspend fun statisticsOrderByDepartment(page: PageQuery, companyId: Int): PageView<Map<String, Any?>> {
        val startTime = firstDay()
        val endTime = LocalDateTime.now()
        val pageSql = page.toPageSql()
        val where = page.where("mdp").and("mdp.company_id").`is`(companyId)
        return getPage(connect.execute("select count(1) as tradesNumber," +
                " COALESCE(sum(mti.trade_amount), 0) as tradesCapacity," +
                " COALESCE(sum(mti.trade_money), 0)  as tradesVolume," +
                " COALESCE(avg(mti.trade_price), 0)  as avgPrice," +
                " COALESCE(min(mti.trade_price), 0)  as minPrice," +
                " COALESCE(max(mti.trade_price), 0)  as maxPrice," +
                " (select md.name from mt_department md where mdp.department_id = md.id)" +
                " from mt_department_post mdp" +
                " left join mt_stockholder ms on mdp.id = ms.dp_id" +
                " left join mt_trade_info mti on ((mti.buyer_id = ms.user_id) or (mti.seller_id = ms.user_id)) and" +
                " mti.trade_time between :startTime and :endTime" +
                " where $where" +
                " group by mdp.department_id $pageSql")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .map { r, _ ->
                    mapOf("tradesNumber" to r.get("tradesNumber", java.lang.Long::class.java)?.toLong(),
                            "tradesCapacity" to r.get("tradesCapacity", java.lang.Long::class.java)?.toLong(),
                            "tradesVolume" to r.get("tradesVolume", BigDecimal::class.java),
                            "avgPrice" to r.get("avgPrice", BigDecimal::class.java),
                            "minPrice" to r.get("minPrice", BigDecimal::class.java),
                            "maxPrice" to r.get("maxPrice", BigDecimal::class.java),
                            "name" to r.get("name", String::class.java))
                }.all(), connect, page, "mt_department_post mdp", where)
    }

    /**
     * 获取指定时间范围的最大和最小报价
     */
    suspend fun findMaxMinPriceByTradeTimeAndRoomId(roomId: String, startTime: LocalDateTime, endTime: LocalDateTime): Map<String, BigDecimal> {
        return connect.execute("select COALESCE(min(trade_price), 0) as minPrice," +
                " COALESCE(max(trade_price), 0) as maxPrice from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and room_id = :roomId ")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("roomId", roomId)
                .map { r, _ ->
                    mapOf("minPrice" to r.get("minPrice", BigDecimal::class.java)!!,
                            "maxPrice" to r.get("maxPrice", BigDecimal::class.java)!!)
                }.awaitOne()
    }

    suspend fun findMaxMinPriceByTradeTimeAndStockId(startTime: LocalDateTime, endTime: LocalDateTime, stockId: Int): Map<String, BigDecimal> {
        return connect.execute("select COALESCE(min(trade_price), 0) as minPrice," +
                " COALESCE(max(trade_price), 0) as maxPrice from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and stock_id = :stockId ")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ ->
                    mapOf("minPrice" to r.get("minPrice", BigDecimal::class.java)!!,
                            "maxPrice" to r.get("maxPrice", BigDecimal::class.java)!!)
                }.awaitOne()
    }

    /**
     * 获取指定时间内的交易量排名
     */
    fun getTradeAmountRank(topNumber: Int, time: LocalDateTime = firstDay()): Mono<List<Map<String, Any?>>> {
        return connect.execute("SELECT ti.company_id, sum(ti.trade_amount) as amount " +
                " , (select count(rr.company_id) as openingNumber from mt_room_record rr where rr.company_id = ti.company_id) " +
                " , (select c.name from mt_company c where c.id = ti.company_id) " +
                " from mt_trade_info ti " +
                " where ti.trade_time > :time" +
                " GROUP BY ti.company_id ORDER BY amount desc limit $topNumber")
                .bind("time", time)
                .map { r, _ ->
                    mapOf("amount" to r.get("amount", java.lang.Long::class.java),
                            "companyId" to r.get("company_id", java.lang.Integer::class.java),
                            "name" to r.get("name", java.lang.Integer::class.java),
                            "openingNumber" to r.get("openingNumber", java.lang.Integer::class.java))
                }.all().collectList()
    }

    /**
     * 获取指定时间内的交易金额排名
     */
    fun getTradeMoneyRank(topNumber: Int, time: LocalDateTime = firstDay()): Mono<List<Map<String, Any?>>> {
        return connect.execute("SELECT ti.company_id, sum(ti.trade_money) as money " +
                " , (select count(rr.company_id) as openingNumber from mt_room_record rr where rr.company_id = ti.company_id) " +
                " , (select c.name from mt_company c where c.id = ti.company_id) " +
                " from mt_trade_info ti " +
                " where ti.trade_time > :time" +
                " GROUP BY ti.company_id ORDER BY money desc limit $topNumber")
                .bind("time", time)
                .map { r, _ ->
                    mapOf("money" to r.get("money", java.lang.Long::class.java),
                            "companyId" to r.get("company_id", java.lang.Integer::class.java),
                            "name" to r.get("name", java.lang.Integer::class.java),
                            "openingNumber" to r.get("openingNumber", java.lang.Integer::class.java))
                }.all().collectList()
    }

    fun outExcel(companyId: Int, date: LocalDate, response: ServerHttpResponse): Mono<Void> {
        val startTime = date.withDayOfMonth(1).atStartOfDay()
        val lastTime = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)
        val where = where("company_id").`is`(companyId).and("trade_time").between(startTime, lastTime)
        val data = connect.select()
                .from<TradeInfo>()
                .matching(where)
                .fetch()
                .all()
                .collectList().block()

        val rowName = arrayOf("时间", "成交状态", "交易模式", "买方", "买方报价", "卖方", "卖方报价", "成交价", "成交数量")
        // 第一步：定义一个新的工作簿
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet()
        val alignStyle = wb.createCellStyle()
        alignStyle.alignment = HorizontalAlignment.CENTER
        sheet.setDefaultColumnStyle(4, alignStyle)
        val rowTitle = sheet.createRow(0)
        for (i in rowName.indices) {
            val cellTitle = rowTitle.createCell(i)
            cellTitle.setCellValue(rowName[i])
        }
        for (i in 0 until data!!.size) {
            val rows = sheet.createRow(i + 1)
            for (key in rowName.indices) {
                val cells = rows.createCell(key)
                cells.setCellValue(getData(key + 1, data[i]))
            }
        }
        // TODO 也许可以直接获取输出流写出，不要存文件
        val formatter = DateTimeFormatter.ofPattern("yyy-MM-ddHHmmss")
        val path = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID() + File.separator + companyId + "-" + lastTime.format(formatter) + ".xlsx"
        logger.info(path)
        val fileExcel = File(path)
        if (!fileExcel.exists()) {
            fileExcel.parentFile.mkdirs()
        }
        val fileOutputStream = FileOutputStream(fileExcel)
        wb.write(fileOutputStream)
        fileOutputStream.close()
        val zeroCopyHttpOutputMessage = response as ZeroCopyHttpOutputMessage
        return try {
            response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                    URLEncoder.encode(fileExcel.name, StandardCharsets.UTF_8.displayName()))
            zeroCopyHttpOutputMessage.writeWith(fileExcel, 0, fileExcel.length())
        } catch (e: UnsupportedEncodingException) {
            throw UnsupportedOperationException()
        }
    }

    fun getData(index: Int, tradeInfo: TradeInfo): String? {
        val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
        return when (index) {
            1 -> tradeInfo.tradeTime?.format(formatter)
            2 -> TradeState.getZhName(tradeInfo.tradeState)
            3 -> RoomEnum.getRoomEnum(tradeInfo.model!!).details
            4 -> tradeInfo.buyerName
            5 -> tradeInfo.buyerPrice?.toEngineeringString()
            6 -> tradeInfo.sellerName
            7 -> tradeInfo.sellerPrice?.toEngineeringString()
            8 -> tradeInfo.tradePrice?.toEngineeringString()
            9 -> tradeInfo.tradeAmount?.toString()
            else -> error("错误，不支持的行号:$index")
        }
    }

}