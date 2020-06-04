package com.mt.mtuser

import com.fasterxml.jackson.databind.ObjectMapper
import com.mt.mtcommon.RoomRecord
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.User
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.service.R2dbcService
import com.mt.mtuser.service.RedisUtil
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.util.*


@SpringBootTest
class MtUserApplicationTests {
    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    protected lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var redisUtil: RedisUtil

    @Test
    fun testRedis() {
        mono {
            val record = redisUtil.deleteAndGetRoomRecord("1")
            println(record?.toString())
        }.block()
    }

    fun nmka2(user: Mono<User>): Mono<Stockholder> {
        return user.filter { !StringUtils.isEmpty(it.phone) && !StringUtils.isEmpty(it.password) }
                .switchIfEmpty(Mono.error(IllegalStateException("请正确填写用户名或密码")))
                .flatMap { println("nmka");Mono.just(0) }
                .filter { it == 0 }
                .switchIfEmpty(Mono.error(IllegalStateException("用户已存在")))
                .flatMap { user }
                .flatMap { ur ->
                    ur.passwordEncoder()
                    ur.id = 2
                    Mono.just(ur)
                }.flatMap { newUser ->
                    val role = Stockholder()
                    role.userId = newUser.id
                    Mono.just(role)
                }
    }

    @Test
    fun testR2dbc() {
        val roomRecord = RoomRecord(id = 1, mode = "E", roomId = "D12")
        r2dbc.dynamicUpdate(roomRecord)
                .matching(Criteria.where("id").`is`(roomRecord.id!!))
                .fetch().rowsUpdated().block()
        mono {
            roomRecordDao.findById(1)
            connect.select().from<RoomRecord>().matching(Criteria.where("id").`is`(1)).fetch().one().awaitSingle()
        }.block()
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
}

