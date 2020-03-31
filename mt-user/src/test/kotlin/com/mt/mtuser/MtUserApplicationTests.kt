package com.mt.mtuser

import com.mt.mtuser.service.room.BaseRoomService
import com.mt.mtuser.service.room.RoomEnum
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class MtUserApplicationTests {
    @Autowired
    private lateinit var baseRoomService: BaseRoomService

    @Test
    fun contextLoads() {
        runBlocking {
            baseRoomService.getNextRoomId(RoomEnum.TIMING)
        }
    }

}
