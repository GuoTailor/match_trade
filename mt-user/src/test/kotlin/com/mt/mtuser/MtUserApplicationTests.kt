package com.mt.mtuser

import com.mt.mtuser.common.plus
import com.mt.mtuser.entity.Role
import com.mt.mtuser.entity.User
import com.mt.mtuser.schedule.QuartzManager
import com.mt.mtuser.schedule.RoomEndJobInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.quartz.JobDataMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
                    role.userid = newUser.id
                    Mono.just(role)
                }
    }

    @Test
    fun testMono() {
        val user = User()
        user.phone = " 123456"
        user.password = "123456"
        val role = nmka2(Mono.just(user)).block()
        println(role)
    }

}
