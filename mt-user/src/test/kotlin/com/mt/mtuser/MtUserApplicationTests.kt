package com.mt.mtuser

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.toDuration
import com.mt.mtcommon.toMillisOfDay
import com.mt.mtuser.entity.Role
import com.mt.mtuser.entity.User
import org.junit.jupiter.api.Test
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.time.LocalTime


//@SpringBootTest
class MtUserApplicationTests {
    /*@Autowired
    private lateinit var quartzManager: QuartzManager

    @Test
    fun contextLoads() {
        runBlocking {
            val localTime = LocalTime.now() + LocalTime.parse("00:00:10")
            val cron = "%d %d %d ? * *".format(localTime.second, localTime.minute, localTime.hour)
            println("nmka-------- $cron")
            var timeLong = System.currentTimeMillis()
            for (i in 0 until 1000) {
                quartzManager.addJob(RoomEndJobInfo(cron, "test - $i", JobDataMap(mapOf("index" to i)), TestTask::class.java))
            }
            println("nmka--------${System.currentTimeMillis() - timeLong}")
            timeLong = System.currentTimeMillis()
            delay(20_000)
            println("nmka--------end")
        }
    }*/

    fun nmka2(user: Mono<User>): Mono<Role> {
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
                    val role = Role()
                    role.userId = newUser.id
                    Mono.just(role)
                }
    }

    @Test
    fun testMono() {
        val jsonStr = "[]"
        val list: List<String> = ObjectMapper().readValue(jsonStr)
        println(list)
    }

    @Test
    fun testTime() {
        val local = LocalTime.now().toMillisOfDay()
        println(local)
        println(System.currentTimeMillis())
    }

    @Test
    fun testNm() {
        val json = ObjectMapper()
        val string = "{\"Message\":\"账户余额不足\",\"RequestId\":\"F84EDD77-C09F-45CC-A850-25CD982B3C98\",\"Code\":\"isv.AMOUNT_NOT_ENOUGH\"}"
        val info = json.readValue(string, Map::class.java)
        println(info)
    }
}

class Info(var message: String? = null, code: String? = null)
