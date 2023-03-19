package com.mt.mtuser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.mt.mtcommon.RoomRecord
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.dao.StockDao
import com.mt.mtuser.entity.*
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.service.*
import com.mt.mtuser.service.kline.KlineService
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.util.ParsingUtils
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*


@SpringBootTest
class MtUserApplicationTests {
    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var redisUtil: RedisUtil

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var stockDao: StockDao

    @Autowired
    lateinit var roomService: RoomService

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var departmentPostService: DepartmentPostService
    @Autowired
    lateinit var klineService: KlineService
    @Autowired
    lateinit var json: ObjectMapper
    @Autowired
    lateinit var userService: UserService

    @Test
    fun testTransactional() {
        runBlocking {
            val user = User()
            user.phone = "123"
            user.password = "123"
            userService.register(user)
        }
    }

    @Test
    fun testQuery() {
        mono {
            val findKlineByStockId = klineService.findKlineByStockId(19, "1m", PageQuery())
            findKlineByStockId.item?.forEach {
                println(json.writeValueAsString(it))
                if (it.id == 811L) {
                    it.id = null
                    klineService.saveKline(it, "mt_1m_kline")
                }
            }
        }.block()
    }

    @Test
    fun testRedis() {
        mono {
            val record = redisUtil.deleteAndGetRoomRecord("1")
            println(record?.toString())
        }.block()
    }

    @Test
    fun testR2dbc() {
        val update = r2dbc.getUpdate(Kline(1, 2, 3, LocalDateTime.now(), 5))
        println(update.toString())
        println(ParsingUtils.reconcatenateCamelCase("companyId", "_"))
    }

    @Test
    fun testMono() {
        val m = mono { nmka() }
        val r = ResponseInfo.ok(m)
                .map {
                    println("nmka")
                    it
                }.doOnError { println("error") }
                .doOnCancel { println("cancel") }
        println(r.block())
    }

    suspend fun nmka(): String? {
        return null
    }

    @Test
    fun testTime() {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, 0)
        c.set(Calendar.DAY_OF_MONTH, 1)//设置为1号,当前日期既为本月第一天
        val monthfirst = Util.createDate(c.time.time)
        println("===============nowfirst:$monthfirst")

        //获取当前月最后一天
        val ca = Calendar.getInstance()
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthlast = Util.createDate(ca.time.time)
        println("===============last:$monthlast")
    }

    @Test
    fun testNm() {
        val json = ObjectMapper()
        val string = "{\"Message\":\"账户余额不足\",\"RequestId\":\"F84EDD77-C09F-45CC-A850-25CD982B3C98\",\"Code\":\"isv.AMOUNT_NOT_ENOUGH\"}"
        val info = json.readValue(string, Map::class.java)
        println(info)
    }

    @Test
    fun testDb() = runBlocking{
        val data = companyService.getShareholderByDepartment(PageQuery(), 1, "技术部")
        data.item?.forEach {
            println(it.toString())
        }
        println("<------------>")
        val data2 = companyService.findAllByQuery(PageQuery())
        data2.item?.forEach {
            println(it.toString())
        }
        val data3 = departmentPostService.findByDpId(1)
        println(data3?.toString())
        delay(10000)
        println(">>>>>>>>>>")
    }
}

